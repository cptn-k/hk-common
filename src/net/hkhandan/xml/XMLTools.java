package net.hkhandan.xml;

import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import java.util.*;
import org.w3c.dom.*;
import java.io.*;

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
public class XMLTools {
    private XMLTools(){
    }

    public static String exceptionToXMLString(Exception e) {
        StringBuffer buff = new StringBuffer();
        buff.append("<EXCEPTION>");
        buff.append("<MESSAGE>");
        buff.append(e.getMessage());
        buff.append("</MESSAGE>");
        buff.append("<STACKTRACE>");
        StackTraceElement[] trace = e.getStackTrace();
        for(int i = 0; i < trace.length; i++) {
            buff.append("<STACK-ELEMENT ");
            buff.append("className=\""  + trace[i].getClassName()   + "\" ");
            buff.append("fileName=\""   + trace[i].getFileName()    + "\" ");
            buff.append("lineNumber=\"" + trace[i].getLineNumber()  + "\" ");
            buff.append("function=\""   + trace[i].getMethodName()  + "\" ");
            buff.append("isNative=\""   + trace[i].isNativeMethod() + "\"");
            buff.append("/>");
        }
        buff.append("</STACKTRACE>");
        buff.append("</EXCEPTION>");
        return buff.toString();
    }

    public static void printXMLFormatted(OutputStream os, Element e) {
        TransformerFactory trnsFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = trnsFactory.newTransformer();
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }

        e.normalize();

        DOMSource source = new DOMSource(e);
        StreamResult result = new StreamResult(os);
        try {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(source, result);
        } catch (TransformerException ex1) {
            ex1.printStackTrace();
        }
    }

    public static String generatePathString(Element e) {
        StringBuffer buf = new StringBuffer();
        Node node;
        while(true) {
            NamedNodeMap attribs = e.getAttributes();
            for(int i = 0; i < attribs.getLength(); i++) {
                Attr attrib = (Attr)attribs.item(i);
                buf.insert(0,
                           "[@" + attrib.getName() + "=" + attrib.getValue() + "]");
            }
            buf.insert(0, "/" + e.getTagName());
            node = e.getParentNode();
            if(node.getNodeType() != 1)
                break;
            e = (Element)e.getParentNode();
        }
        return buf.toString();
    }


    private static class TmpNodeList implements NodeList {
        private Node[] nodes;
        public TmpNodeList(Vector<Node> nodes) {
            this.nodes = new Node[nodes.size()];
            this.nodes = nodes.toArray(this.nodes);
        }

        public Node item(int index) {
            return nodes[index];
        }

        public int getLength() {
            return nodes.length;
        }
    }

    public static NodeList retriveChildElements(Element e, String name) {
        Vector<Node> selectedNodes = new Vector<Node>();
        NodeList nodes = e.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == node.ELEMENT_NODE)
                if(((Element)node).getTagName().equals(name))
                    selectedNodes.add(node);
        }
        return new TmpNodeList(selectedNodes);
    }
}
