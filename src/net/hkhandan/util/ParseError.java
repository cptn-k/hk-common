package net.hkhandan.util;


import net.hkhandan.xml.XMLTools;
import org.w3c.dom.*;

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
public class ParseError extends Error {
    public ParseError() {
        super("Bad semantics.");
    }

    public ParseError(String message) {
        super("Bad semantics:" + message);
    }

    public ParseError(String message, Element e) {
        super("Bad semantics:\"" + message + "\" at <xpath>" + XMLTools.generatePathString(e) + "</xpath>");
    }
}
