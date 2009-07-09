/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.jaxb;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.jaxb.compiler.*;
import org.eclipse.persistence.jaxb.javamodel.reflection.JavaModelImpl;
import org.eclipse.persistence.jaxb.javamodel.reflection.JavaModelInputImpl;
import org.eclipse.persistence.jaxb.xmlmodel.JavaType;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings;
import org.eclipse.persistence.internal.jaxb.JaxbClassLoader;
import org.eclipse.persistence.internal.jaxb.SessionEventListener;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.exceptions.EclipseLinkException;
import org.eclipse.persistence.exceptions.SessionLoaderException;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.sessions.Project;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose:</b>A TopLink specific JAXBContextFactory. This class can be specified in a
 * jaxb.properties file to make use of TopLink's JAXB 2.0 implementation.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Create a JAXBContext from an array of Classes and a Properties object</li>
 * <li>Create a JAXBContext from a context path and a classloader</li>
 * </ul>
 * <p>
 * This class is the entry point into in TopLink's JAXB 2.0 Runtime. It provides the required
 * factory methods and is invoked by javax.xml.bind.JAXBContext.newInstance() to create new
 * instances of JAXBContext. When creating a JAXBContext from a contextPath, the list of classes is
 * derived either from an ObjectFactory class (schema-to-java) or a jaxb.index file
 * (java-to-schema).
 * 
 * @author mmacivor
 * @since Oracle TopLink 11.1.1.0.0
 * @see javax.xml.bind.JAXBContext
 * @see org.eclipse.persistence.jaxb.JAXBContext
 * @see org.eclipse.persistence.jaxb.compiler.Generator
 */
public class JAXBContextFactory {
    public static final String ECLIPSELINK_OXM_XML_KEY = "eclipselink-oxm-xml";
    private static final String METADATA_MODEL_PACKAGE = "org.eclipse.persistence.jaxb.xmlmodel";

    public static javax.xml.bind.JAXBContext createContext(Class[] classesToBeBound, java.util.Map properties) throws JAXBException {
        ClassLoader loader = null;
        if (classesToBeBound.length > 0) {
            loader = classesToBeBound[0].getClassLoader();
            if (null == loader) {
                loader = Thread.currentThread().getContextClassLoader();
            }
        }
        return createContext(classesToBeBound, properties, loader);
    }

    public static javax.xml.bind.JAXBContext createContext(Class[] classesToBeBound, java.util.Map properties, ClassLoader classLoader) throws JAXBException {
        // Check properties map for eclipselink-oxm.xml entries
        Map<String, XmlBindings> xmlBindings = getXmlBindingsFromProperties(properties, classLoader);
        if (xmlBindings == null) {
            // No properties entry, check package(s) for eclipselink-oxm.xml file(s)
            xmlBindings = getXmlBindingsFromPackages(classesToBeBound, classLoader);
        }
        if (xmlBindings != null) {
            for (String key : xmlBindings.keySet()) {
                classesToBeBound = getXmlBindingsClasses(xmlBindings.get(key), classLoader, classesToBeBound);
            }
        }
        JaxbClassLoader loader = new JaxbClassLoader(classLoader);
        classesToBeBound = updateClassesWithObjectFactory(classesToBeBound, loader);
        try {
            Generator generator = new Generator(new JavaModelInputImpl(classesToBeBound, new JavaModelImpl(loader)), xmlBindings, classLoader);
            return createContext(generator, properties, classLoader, loader, classesToBeBound);
        } catch (Exception ex) {
            throw new JAXBException(ex.getMessage(), ex);
        }
    }

    public static javax.xml.bind.JAXBContext createContext(String contextPath, ClassLoader classLoader) throws JAXBException {
        return createContext(contextPath, classLoader, null);
    }

    public static javax.xml.bind.JAXBContext createContext(String contextPath, ClassLoader classLoader, java.util.Map properties) throws JAXBException {
        EclipseLinkException sessionLoadingException = null;
        try {
            XMLContext xmlContext = new XMLContext(contextPath, classLoader);
            return new org.eclipse.persistence.jaxb.JAXBContext(xmlContext);
        } catch (ValidationException vex) {
            if (vex.getErrorCode() != ValidationException.NO_SESSIONS_XML_FOUND) {
                sessionLoadingException = vex;
            }
        } catch (SessionLoaderException ex) {
            sessionLoadingException = ex;
        } catch (Exception ex) {
            throw new JAXBException(ex);
        }
        ArrayList<Class> classes = new ArrayList<Class>();

        // don't check the context path if the properties map contains metadata override entries
        boolean shouldSearchContextPath = false;
        Map<String, XmlBindings> xmlBindingMap = getXmlBindingsFromProperties(properties, classLoader);
        if (xmlBindingMap == null) {
            shouldSearchContextPath = true;
            xmlBindingMap = new HashMap<String, XmlBindings>();
        } else {
            classes = getXmlBindingsClassesFromMap(xmlBindingMap, classLoader, classes);
        }

        StringTokenizer tokenizer = new StringTokenizer(contextPath, ":");
        XmlBindings xmlBindings = null;
        while (tokenizer.hasMoreElements()) {
            String path = tokenizer.nextToken();
            try {
                Class objectFactory = classLoader.loadClass(path + ".ObjectFactory");
                if (isJAXB2ObjectFactory(objectFactory)) {
                    classes.add(objectFactory);
                }
            } catch (Exception ex) {
                // if there's no object factory, don't worry about it. Check for jaxb.index next
            }
            try {
                // try to load package info just to be safe
                classLoader.loadClass(path + ".package-info");
            } catch (Exception ex) {
            }
            // Next check for a jaxb.index file in case there's one available
            InputStream jaxbIndex = classLoader.getResourceAsStream(path.replace('.', '/') + "/jaxb.index");
            if (jaxbIndex != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(jaxbIndex));
                try {
                    String line = reader.readLine();
                    while (line != null) {
                        String className = path + "." + line.trim();
                        try {
                            classes.add(classLoader.loadClass(className));
                        } catch (Exception ex) {
                            // just ignore for now if the class isn't available.
                        }
                        line = reader.readLine();
                    }
                } catch (Exception ex) {
                }
            }
            // look for an eclipselink-oxm metadata file to process if none were found in properties
            // map
            if (shouldSearchContextPath) {
                InputStream iStream = classLoader.getResourceAsStream(path.replace('.', '/') + "/eclipselink-oxm.xml");
                if (iStream != null) {
                    xmlBindings = getXmlBindings(new StreamSource(iStream), classLoader);
                    classes = getXmlBindingsClasses(xmlBindings, classLoader, classes);
                    xmlBindingMap.put(path, xmlBindings);
                }
            }
        }
        if (classes.size() == 0) {
            org.eclipse.persistence.exceptions.JAXBException jaxbException = org.eclipse.persistence.exceptions.JAXBException.noObjectFactoryOrJaxbIndexInPath(contextPath);
            if (sessionLoadingException != null) {
                jaxbException.setInternalException(sessionLoadingException);
            }
            throw new JAXBException(jaxbException);
        }
        Class[] classArray = new Class[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            classArray[i] = (Class) classes.get(i);
        }
        return createContext(classArray, properties, classLoader, xmlBindingMap);
    }

    public static javax.xml.bind.JAXBContext createContext(Type[] typesToBeBound, java.util.Map properties, ClassLoader classLoader) throws JAXBException {
        // Check properties map for eclipselink-oxm.xml entries
        Map<String, XmlBindings> xmlBindings = getXmlBindingsFromProperties(properties, classLoader);
        if (xmlBindings == null) {
            // No properties entry, check package(s) for eclipselink-oxm.xml file(s)
            xmlBindings = getXmlBindingsFromPackages(typesToBeBound, classLoader);
        }
        if (xmlBindings != null) {
            for (String key : xmlBindings.keySet()) {
                typesToBeBound = getXmlBindingsClasses(xmlBindings.get(key), classLoader, typesToBeBound);
            }
        }
        JaxbClassLoader loader = new JaxbClassLoader(classLoader);
        JavaModelInputImpl inputImpl = new JavaModelInputImpl(typesToBeBound, new JavaModelImpl(loader));
        typesToBeBound = updateTypesWithObjectFactory(typesToBeBound, loader);
        try {
            Generator generator = new Generator(inputImpl, inputImpl.getJavaClassToType(), xmlBindings, classLoader);
            return createContext(generator, properties, classLoader, loader, typesToBeBound);
        } catch (Exception ex) {
            throw new JAXBException(ex.getMessage(), ex);
        }
    }

    private static javax.xml.bind.JAXBContext createContext(Class[] classesToBeBound, java.util.Map properties, ClassLoader classLoader, Map<String, XmlBindings> xmlBindings) throws JAXBException {
        JaxbClassLoader loader = new JaxbClassLoader(classLoader);
        try {
            Generator generator = new Generator(new JavaModelInputImpl(classesToBeBound, new JavaModelImpl(loader)), xmlBindings, loader);
            return createContext(generator, properties, classLoader, loader, classesToBeBound);
        } catch (Exception ex) {
            throw new JAXBException(ex.getMessage(), ex);
        }
    }

    private static javax.xml.bind.JAXBContext createContext(Generator generator, java.util.Map properties, ClassLoader classLoader, JaxbClassLoader loader, Type[] typesToBeBound) throws Exception {
        javax.xml.bind.JAXBContext jaxbContext = null;
        XMLContext xmlContext = null;

        Project proj = generator.generateProject();
        ConversionManager conversionManager = null;
        if (classLoader != null) {
            conversionManager = new ConversionManager();
            conversionManager.setLoader(loader);
        } else {
            conversionManager = ConversionManager.getDefaultManager();
        }
        proj.convertClassNamesToClasses(conversionManager.getLoader());
        // need to make sure that the java class is set properly on each
        // descriptor when using java classname - req'd for JOT api implementation
        for (Iterator<ClassDescriptor> descriptorIt = proj.getOrderedDescriptors().iterator(); descriptorIt.hasNext();) {
            ClassDescriptor descriptor = descriptorIt.next();
            if (descriptor.getJavaClass() == null) {
                descriptor.setJavaClass(conversionManager.convertClassNameToClass(descriptor.getJavaClassName()));
            }
        }

        // disable instantiation policy validation during descriptor initialization
        SessionEventListener eventListener = new SessionEventListener();
        eventListener.setShouldValidateInstantiationPolicy(false);

        xmlContext = new XMLContext(proj, loader, eventListener);
        jaxbContext = new org.eclipse.persistence.jaxb.JAXBContext(xmlContext, generator, typesToBeBound);

        return jaxbContext;
    }

    private static boolean isJAXB2ObjectFactory(Class objectFactoryClass) {
        try {
            Class xmlRegistry = PrivilegedAccessHelper.getClassForName("javax.xml.bind.annotation.XmlRegistry");
            if (objectFactoryClass.isAnnotationPresent(xmlRegistry)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Convenience method for processing a properties map and creating a map of package names to
     * XmlBindings instances.
     * 
     * It is assumed that: - the given map's key will be ECLIPSELINK_OXM_XML_KEY - the value will be
     * Map<String, Source>, where String = package, Source = metadata file
     * 
     * @param properties
     * @param classLoader
     * @return
     */
    private static Map<String, XmlBindings> getXmlBindingsFromProperties(Map properties, ClassLoader classLoader) {
        if (properties != null) {
            Map<String, XmlBindings> bindings = new HashMap<String, XmlBindings>();
            Map<String, Source> metadataFiles = null;
            try {
                metadataFiles = (Map<String, Source>) properties.get(ECLIPSELINK_OXM_XML_KEY);
            } catch (ClassCastException x) {
                // TODO: throw new runtime exception (parameter incorrect)
            }
            if (metadataFiles != null) {
                Source metadataSource;
                for (String key : metadataFiles.keySet()) {
                    metadataSource = metadataFiles.get(key);
                    XmlBindings binding = getXmlBindings(metadataSource, classLoader);
                    if (binding != null) {
                        bindings.put(key, binding);
                    }
                }
            }
            if (bindings.size() > 0) {
                return bindings;
            }
        }
        return null;
    }

    /**
     * Convenience method for processing a Class array and creating a map of package names to
     * XmlBindings instances. Each package will be checked for an eclipselink-oxm.xml file.
     * 
     * @param classes
     * @param classLoader
     * @return
     */
    private static Map<String, XmlBindings> getXmlBindingsFromPackages(Class[] classes, ClassLoader classLoader) {
        if (classes != null) {
            Map<String, XmlBindings> bindings = new HashMap<String, XmlBindings>();
            for (Class cls : classes) {
                String packageName = "";
                if (cls.getPackage() != null) {
                    packageName = cls.getPackage().getName();
                } else {
                    // dynamically generated classes don't have a package - use brute force
                    String className = cls.getName();
                    int idx = className.lastIndexOf('.');
                    if (idx > 0) {
                        packageName = className.substring(0, idx);
                    }
                }

                // only need to process a package once
                if (bindings.containsKey(packageName)) {
                    continue;
                }
                try {
                    InputStream iStream = classLoader.getResourceAsStream(packageName.replace('.', '/') + "/eclipselink-oxm.xml");
                    if (iStream == null) {
                        continue;
                    }
                    Source metadataSource = new StreamSource(iStream);
                    XmlBindings binding = getXmlBindings(metadataSource, classLoader);
                    if (binding != null) {
                        bindings.put(packageName, binding);
                    }
                } catch (Exception e) {
                    // TODO: throw new runtime exception
                }
            }
            if (bindings.size() > 0) {
                return bindings;
            }
        }
        return null;
    }

    /**
     * Convenience method for processing a Type array and creating a map of package names to
     * XmlBindings instances. Each package will be checked for an eclipselink-oxm.xml file.
     * 
     * @param types
     * @param classLoader
     * @return
     */
    private static Map<String, XmlBindings> getXmlBindingsFromPackages(Type[] types, ClassLoader classLoader) {
        if (types != null) {
            Map<String, XmlBindings> bindings = new HashMap<String, XmlBindings>();
            for (Type type : types) {
                String packageName = "";
                if (type.getClass().getPackage() != null) {
                    packageName = type.getClass().getPackage().getName();
                } else {
                    // dynamically generated classes don't have a package - use brute force
                    String className = type.getClass().getName();
                    int idx = className.lastIndexOf('.');
                    if (idx > 0) {
                        packageName = className.substring(0, idx);
                    }
                }

                // only need to process a package once
                if (bindings.containsKey(packageName)) {
                    continue;
                }
                try {
                    InputStream iStream = classLoader.getResourceAsStream(packageName.replace('.', '/') + "/eclipselink-oxm.xml");
                    if (iStream == null) {
                        continue;
                    }
                    Source metadataSource = new StreamSource(iStream);
                    XmlBindings binding = getXmlBindings(metadataSource, classLoader);
                    if (binding != null) {
                        bindings.put(packageName, binding);
                    }
                } catch (Exception e) {
                    // TODO: throw new runtime exception
                }
            }
            if (bindings.size() > 0) {
                return bindings;
            }
        }
        return null;
    }

    /**
     * Convenience method for creating an XmlBindings object based on a given Source. The method
     * will load the eclipselink metadata model and unmarshal the Source. This assumes that the
     * Source represents the eclipselink-oxm.xml metadata file to be unmarshalled.
     * 
     * @param metadataSource
     * @param classLoader
     * @return
     */
    private static XmlBindings getXmlBindings(Source metadataSource, ClassLoader classLoader) {
        XmlBindings xmlBindings = null;
        if (metadataSource != null) {
            JAXBContext jaxbContext;
            Unmarshaller unmarshaller;
            try {
                jaxbContext = (JAXBContext) createContext(METADATA_MODEL_PACKAGE, classLoader);
                unmarshaller = jaxbContext.createUnmarshaller();
                xmlBindings = (XmlBindings) unmarshaller.unmarshal(metadataSource);
            } catch (JAXBException jaxbEx) {
                // TODO: throw new runtime exception
            }
        }
        return xmlBindings;
    }

    /**
     * Convenience method that returns an array of Types based on a given XmlBindings. The resulting
     * array will not contain duplicate entries.
     * 
     * @param xmlBindings
     * @param classLoader
     * @param existingTypes
     * @return
     */
    private static Type[] getXmlBindingsClasses(XmlBindings xmlBindings, ClassLoader classLoader, Type[] existingTypes) {
        Type[] additionalTypes = existingTypes;

        ArrayList<Class> javaTypeClasses = new ArrayList<Class>();
        for (JavaType javaType : xmlBindings.getJavaTypes().getJavaType()) {
            try {
                javaTypeClasses.add(classLoader.loadClass(javaType.getName()));
            } catch (ClassNotFoundException e) {
                // TODO: throw new runtime exception
            }
        }

        if (javaTypeClasses.size() > 0) {
            // add any existing classes not defined in the metadata file to the list
            for (Type type : existingTypes) {
                // ignore ParameterizedTypes
                if (type instanceof Class) {
                    Class cls = (Class) type;
                    if (!javaTypeClasses.contains(cls)) {
                        javaTypeClasses.add(cls);
                    }
                }
            }
            // populate the array to return
            additionalTypes = javaTypeClasses.toArray(new Type[javaTypeClasses.size()]);
        }
        return additionalTypes;
    }

    /**
     * Convenience method that returns an array of Classes based on a given XmlBindings and an array
     * of existing classes. The resulting array will not contain duplicate entries.
     * 
     * @param xmlBindings
     * @param classLoader
     * @param existingClasses
     * @return
     */
    private static Class[] getXmlBindingsClasses(XmlBindings xmlBindings, ClassLoader classLoader, Class[] existingClasses) {
        Class[] additionalClasses = existingClasses;
        ArrayList<Class> javaTypeClasses = new ArrayList<Class>();
        for (JavaType javaType : xmlBindings.getJavaTypes().getJavaType()) {
            try {
                javaTypeClasses.add(classLoader.loadClass(javaType.getName()));
            } catch (ClassNotFoundException e) {
                // TODO: throw new runtime exception
            }
        }

        if (javaTypeClasses.size() > 0) {
            // add any existing classes not defined in the metadata file to the list
            for (Class cls : existingClasses) {
                if (!javaTypeClasses.contains(cls)) {
                    javaTypeClasses.add(cls);
                }
            }
            // populate the array to return
            additionalClasses = javaTypeClasses.toArray(new Class[javaTypeClasses.size()]);
        }
        return additionalClasses;
    }

    /**
     * Convenience method that returns a list of Classes based on a given XmlBindings and an array
     * of existing classes. The resulting array will not contain duplicate entries.
     * 
     * @param xmlBindings
     * @param classLoader
     * @param existingClasses
     * @return
     */
    private static ArrayList<Class> getXmlBindingsClasses(XmlBindings xmlBindings, ClassLoader classLoader, ArrayList<Class> existingClasses) {
        ArrayList<Class> additionalClasses = existingClasses;
        for (JavaType javaType : xmlBindings.getJavaTypes().getJavaType()) {
            try {
                Class jClass = classLoader.loadClass(javaType.getName());
                if (!additionalClasses.contains(jClass)) {
                    additionalClasses.add(jClass);
                }
            } catch (ClassNotFoundException e) {
                // TODO: throw new runtime exception
            }
        }
        return additionalClasses;
    }

    /**
     * Convenience method that returns an array of Classes based on a map given XmlBindings and an
     * array of existing classes. The resulting array will not contain duplicate entries.
     * 
     * @param xmlBindingMap
     * @param classLoader
     * @param existingClasses
     * @return
     */
    private static ArrayList<Class> getXmlBindingsClassesFromMap(Map<String, XmlBindings> xmlBindingMap, ClassLoader classLoader, ArrayList<Class> existingClasses) {
        ArrayList<Class> additionalClasses = existingClasses;
        // for each xmlBindings
        for (String packageName : xmlBindingMap.keySet()) {
            additionalClasses = getXmlBindingsClasses(xmlBindingMap.get(packageName), classLoader, additionalClasses);
        }
        return additionalClasses;
    }

    private static Class[] updateClassesWithObjectFactory(Class[] classes, ClassLoader loader) {
        ArrayList<Class> updatedClasses = new ArrayList<Class>();
        for (Class next : classes) {
            if (!(updatedClasses.contains(next))) {
                updatedClasses.add(next);
            }
            if (next.getPackage() != null) {
                String packageName = next.getPackage().getName();
                try {
                    Class objectFactoryClass = loader.loadClass(packageName + ".ObjectFactory");
                    if (!(updatedClasses.contains(objectFactoryClass))) {
                        updatedClasses.add(objectFactoryClass);
                    }
                } catch (Exception ex) {
                }
            }
        }

        return updatedClasses.toArray(new Class[updatedClasses.size()]);
    }

    private static Type[] updateTypesWithObjectFactory(Type[] types, ClassLoader loader) {
        ArrayList<Type> updatedTypes = new ArrayList<Type>();
        for (Type next : types) {
            if (!(updatedTypes.contains(next))) {
                updatedTypes.add(next);
            }
            if (next instanceof Class) {
                if (((Class) next).getPackage() != null) {
                    String packageName = ((Class) next).getPackage().getName();
                    try {
                        Class objectFactoryClass = loader.loadClass(packageName + ".ObjectFactory");
                        if (!(updatedTypes.contains(objectFactoryClass))) {
                            updatedTypes.add(objectFactoryClass);
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }
        return updatedTypes.toArray(new Type[updatedTypes.size()]);
    }
}
