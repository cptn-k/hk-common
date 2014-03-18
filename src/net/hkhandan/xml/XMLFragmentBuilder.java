package net.hkhandan.xml;

import org.w3c.dom.*;
import java.util.*;

/**
 * <p>Title: Object Recogniser</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author Hamed Khandan
 * @version 1.0
 */
public class XMLFragmentBuilder {
    Element root;
    Document doc;
    public XMLFragmentBuilder(Element e) {
        root = e;
        doc = e.getOwnerDocument();
    }

    public void appendAttrib(String name, String value) {
        root.setAttribute(name, value);
    }

    public void appendElement(String name, String value) {
        Element e = doc.createElement(name);
        e.setNodeValue(value);
    }

    public void appendElement(String name, String value, String attribName, String attribValue) {
        Element e = doc.createElement(name);
        if(value != null)
            e.setTextContent(value);
        if(attribName != null && attribValue != null) {
            e.setAttribute(attribName, attribValue);
        }
        root.appendChild(e);
    }

    public void appendElements(String name, String[] values) {
        for(int i = 0; i < values.length; i++)
            appendElement(name, values[i]);
    }

    public void appendElements(String name, String values[], String attribName,
                               String[] attribValues)
    {
        for(int i = 0; i < attribValues.length; i++)
            appendElement(name, (values == null)?null:values[i],
                    attribName, (attribValues == null)?null:attribValues[i]);
    }

    public void appendElement(XMLFriendly object) {
        root.appendChild(object.convertToXMLElement(doc));
    }

    public void appendElements(XMLFriendly[] objects) {
        for(int i = 0; i < objects.length; i++)
            appendElement(objects[i]);
    }

    public void appendElements(Enumeration objectList) {
        while(objectList.hasMoreElements())
            appendElement((XMLFriendly)objectList.nextElement());
    }
}


