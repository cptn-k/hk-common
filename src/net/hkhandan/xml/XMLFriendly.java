package net.hkhandan.xml;

import net.hkhandan.util.ParseError;
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
public interface XMLFriendly extends java.io.Serializable {
    public Element convertToXMLElement(Document doc);
    public void convertFromXMLElement(Element e) throws ParseError;
}
