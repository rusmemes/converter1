package ru.ewromet.converter2.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SymFileParser {

    private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

    public static RadanCompoundDocument parse(String symFilePath) throws ParserConfigurationException, SAXException, IOException {

        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        XmlReaderHandler handler = new XmlReaderHandler();
        xmlReader.setContentHandler(handler);
        xmlReader.parse(symFilePath);

        return handler.getRadanCompoundDocument();
    }

    private static class XmlReaderHandler extends DefaultHandler {

        private static final String RADAN_COMPOUND_DOCUMENT_TAG = "RadanCompoundDocument";
        private static final String RADAN_ATTRIBUTES_TAG = "RadanAttributes";
        private static final String GROUP_TAG = "Group";
        private static final String ATTR_TAG = "Attr";
        private static final String VALID_TAG = "Valid";
        private static final String MC_TAG = "MC";
        private static final String QUOTATION_INFO_TAG = "QuotationInfo";
        private static final String INFO_TAG = "Info";
        private static final String TOOL_TAG = "Tool";
        private static final String CUT_COUND_TAG = "CutCond";
        private static final String PIERCE_TAG = "Pierce";

        private static final String ATTRIBUTE_CLASS = "class";
        private static final String ATTRIBUTE_NAME = "name";
        private static final String ATTRIBUTE_DESC = "desc";
        private static final String ATTRIBUTE_TYPE = "type";
        private static final String ATTRIBUTE_ORD = "ord";
        private static final String ATTRIBUTE_VALUE = "value";
        private static final String ATTRIBUTE_NUM = "num";
        private static final String ATTRIBUTE_PERM = "perm";
        private static final String ATTRIBUTE_MACHINE = "machine";
        private static final String ATTRIBUTE_MAX = "max";
        private static final String ATTRIBUTE_MIN = "min";
        private static final String ATTRIBUTE_EXPR = "expr";
        private static final String ATTRIBUTE_LENGTH = "length";
        private static final String ATTRIBUTE_TIME = "time";
        private static final String ATTRIBUTE_CUTOUT_AREA = "cutoutArea";
        private static final String ATTRIBUTE_DESCRIPTION = "description";

        private RadanCompoundDocument radanCompoundDocument;
        private Group currentGroup;
        private Attr currentAttr;
        private Info currentInfo;
        private MC currentMC;

        public RadanCompoundDocument getRadanCompoundDocument() {
            return radanCompoundDocument;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case RADAN_COMPOUND_DOCUMENT_TAG:
                    radanCompoundDocument = new RadanCompoundDocument();
                    break;
                case RADAN_ATTRIBUTES_TAG:
                    radanCompoundDocument.setRadanAttributes(new RadanAttributes());
                    break;
                case QUOTATION_INFO_TAG:
                    radanCompoundDocument.setQuotationInfo(new QuotationInfo());
                    break;
                case GROUP_TAG:
                    RadanAttributes radanAttributes = radanCompoundDocument.getRadanAttributes();

                    List<Group> groups = radanAttributes.getGroups();
                    if (groups == null) {
                        groups = new ArrayList<>();
                        radanAttributes.setGroups(groups);
                    }

                    Group group = new Group();
                    groups.add(group);

                    group.setKlass(attributes.getValue(ATTRIBUTE_CLASS));
                    group.setName(attributes.getValue(ATTRIBUTE_NAME));
                    group.setDesc(attributes.getValue(ATTRIBUTE_DESC));

                    currentGroup = group;
                    break;
                case ATTR_TAG:
                    List<Attr> attrs = currentGroup.getAttrs();
                    if (attrs == null) {
                        attrs = new ArrayList<>();
                        currentGroup.setAttrs(attrs);
                    }
                    Attr attr = new Attr();
                    attrs.add(attr);

                    attr.setValue(attributes.getValue(ATTRIBUTE_VALUE));
                    attr.setDesc(attributes.getValue(ATTRIBUTE_DESC));
                    attr.setName(attributes.getValue(ATTRIBUTE_NAME));
                    attr.setNum(attributes.getValue(ATTRIBUTE_NUM));
                    attr.setOrd(attributes.getValue(ATTRIBUTE_ORD));
                    attr.setType(attributes.getValue(ATTRIBUTE_TYPE));
                    attr.setType(attributes.getValue(ATTRIBUTE_TYPE));

                    currentAttr = attr;
                    break;
                case VALID_TAG:
                    List<Valid> valids = currentAttr.getValids();
                    if (valids == null) {
                        valids = new ArrayList<>();
                        currentAttr.setValids(valids);
                    }
                    Valid valid = new Valid();
                    valids.add(valid);

                    valid.setExpr(attributes.getValue(ATTRIBUTE_EXPR));
                    valid.setMax(attributes.getValue(ATTRIBUTE_MAX));
                    valid.setMin(attributes.getValue(ATTRIBUTE_MIN));
                    valid.setPerm(attributes.getValue(ATTRIBUTE_PERM));
                    break;
                case MC_TAG:
                    List<MC> mcs = null;
                    if (currentAttr != null) {
                        mcs = currentAttr.getMcs();
                        if (mcs == null) {
                            mcs = new ArrayList<>();
                            currentAttr.setMcs(mcs);
                        }
                    } else if (currentInfo != null) {
                        mcs = currentInfo.getMcs();
                        if (mcs == null) {
                            mcs = new ArrayList<>();
                            currentInfo.setMcs(mcs);
                        }
                    }
                    if (mcs == null) {
                        return;
                    }

                    MC mc = new MC();
                    mcs.add(mc);

                    mc.setMachine(attributes.getValue(ATTRIBUTE_MACHINE));
                    mc.setValue(attributes.getValue(ATTRIBUTE_VALUE));

                    currentMC = mc;
                    break;
                case INFO_TAG:
                    QuotationInfo quotationInfo = radanCompoundDocument.getQuotationInfo();
                    List<Info> infos = quotationInfo.getInfos();
                    if (infos == null) {
                        infos = new ArrayList<>();
                        quotationInfo.setInfos(infos);
                    }
                    Info info = new Info();
                    infos.add(info);

                    info.setName(attributes.getValue(ATTRIBUTE_NAME));
                    info.setNum(attributes.getValue(ATTRIBUTE_NUM));
                    info.setValue(attributes.getValue(ATTRIBUTE_VALUE));

                    currentInfo = info;
                    break;
                case TOOL_TAG:
                    List<Tool> tools = currentMC.getTools();
                    if (tools == null) {
                        tools = new ArrayList<>();
                        currentMC.setTools(tools);
                    }
                    Tool tool = new Tool();
                    tools.add(tool);

                    tool.setCutoutArea(attributes.getValue(ATTRIBUTE_CUTOUT_AREA));
                    tool.setLength(attributes.getValue(ATTRIBUTE_LENGTH));
                    tool.setName(attributes.getValue(ATTRIBUTE_NAME));
                    tool.setTime(attributes.getValue(ATTRIBUTE_TIME));
                    break;
                case CUT_COUND_TAG:
                    List<CutCond> cutConds = currentMC.getCutConds();
                    if (cutConds == null) {
                        cutConds = new ArrayList<>();
                        currentMC.setCutConds(cutConds);
                    }
                    CutCond cutCond = new CutCond();
                    cutConds.add(cutCond);

                    cutCond.setDescription(attributes.getValue(ATTRIBUTE_DESCRIPTION));
                    cutCond.setLength(attributes.getValue(ATTRIBUTE_LENGTH));
                    cutCond.setName(attributes.getValue(ATTRIBUTE_NAME));
                    cutCond.setNum(attributes.getValue(ATTRIBUTE_NUM));
                    cutCond.setTime(attributes.getValue(ATTRIBUTE_TIME));
                    break;
                case PIERCE_TAG:
                    List<Pierce> pierces = currentMC.getPierces();
                    if (pierces == null) {
                        pierces = new ArrayList<>();
                        currentMC.setPierces(pierces);
                    }
                    Pierce pierce = new Pierce();
                    pierces.add(pierce);

                    pierce.setDescription(attributes.getValue(ATTRIBUTE_DESCRIPTION));
                    pierce.setNum(attributes.getValue(ATTRIBUTE_NUM));
                    pierce.setTime(attributes.getValue(ATTRIBUTE_TIME));
                    pierce.setType(attributes.getValue(ATTRIBUTE_TYPE));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case GROUP_TAG:
                    currentGroup = null;
                    break;
                case ATTR_TAG:
                    currentAttr = null;
                    break;
                case MC_TAG:
                    currentMC = null;
                    break;
                case INFO_TAG:
                    currentInfo = null;
                    break;
                default:
                    break;
            }
        }
    }
}
