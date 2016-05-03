package com.xcode.mobile.smilealarm;

import android.os.Environment;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.xcode.mobile.smilealarm.alarmpointmanager.AlarmPoint;
import com.xcode.mobile.smilealarm.alarmpointmanager.ReturnCode;
import com.xcode.mobile.smilealarm.listmanager.Task;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesHandler;
import com.xcode.mobile.smilealarm.tunemanager.Tune;

public class DataHelper {

    /* FOR XML */
    private static final String RISING_PLAN = "rPn";
    private static final String ALARM_POINTs = "aPs";
    private static final String ALARM_POINT = "aP";
    private static final String DATE = "id";
    private static final String TIME_01 = "t01";
    private static final String TIME_02 = "t02";
    private static final String AP_TUNE_ID = "tId";
    private static final String FADE_IN = "fIn";
    private static final String PROTECTED = "prt";
    private static final String COLOR = "clr";
    private static final String REPEAT = "rpt";
    private static final String REPEAT_ID = "rID";

    private static final String TUNES = "tns";
    private static final String TUNE = "tn";
    private static final String TUNE_KEY = "id";
    private static final String TUNE_PATH = "path";

    private static final String TODO_LIST = "tds";
    private static final String TASK = "tsk";
    private static final String TASK_ID = "id";
    private static final String TASK_NAME = "n";
    private static final String TASK_TIME = "t";
    private static final String TASK_DATE = "d";

    private static final String FOLDER_NAME = "rsearly_files";
    private static final String XML_FILE_ALARM_POINT_LIST = "apl.xml";
    private static final String XML_FILE_RISING_PLAN = "rp.xml";
    private static final String XML_FILE_TO_DO_LIST = "todo.xml";
    private static final String XML_FILE_USER_TUNES = "rect.xml";

    private static HashMap<Date, AlarmPoint> RisingPlan;
    private static HashMap<Date, AlarmPoint> AlarmPointList;

    private static DataHelper _instance = new DataHelper();

    public static DataHelper getInstance() {
        return _instance;
    }

    public HashMap<Date, AlarmPoint> getAlarmPointListFromData(Boolean isRisingPlan) {

        String filename;
        if (isRisingPlan) {
            if (RisingPlan != null)
                return RisingPlan;
            filename = XML_FILE_RISING_PLAN;
        } else {
            if (AlarmPointList != null)
                return AlarmPointList;
            filename = XML_FILE_ALARM_POINT_LIST;
        }

        HashMap<Date, AlarmPoint> listFromDataBase = null;

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/" + FOLDER_NAME);
            myDir.mkdirs();
            File file = new File(myDir, filename);

            listFromDataBase = new HashMap<Date, AlarmPoint>();
            if (!file.exists()) {
            } else {
                FileInputStream fistream = new FileInputStream(file);
                Document doc = docBuilder.parse(fistream);

                // optional, but recommended
                doc.getDocumentElement().normalize();
                NodeList nAlarmPointList = doc.getElementsByTagName(ALARM_POINT);

                for (int i = 0; i < nAlarmPointList.getLength(); i++) {
                    Node nodeAlarmPoint = nAlarmPointList.item(i);
                    long millisDate = 0, millisTime01 = 0, millisTime02 = 0;
                    String stringTuneId = "";
                    Boolean _fadeIn = false, _protected = false, _color = false;
                    List<Boolean> _repeat = null;
                    int _repeatID = 0;

                    // parse data for alarm point
                    if (nodeAlarmPoint.getNodeType() == Node.ELEMENT_NODE) {

                        Element elementAlarmPoint = (Element) nodeAlarmPoint;

                        millisDate = Long.parseLong(elementAlarmPoint.getAttribute(DATE));
                        millisTime01 = Long.parseLong(elementAlarmPoint.getAttribute(TIME_01));
                        millisTime02 = Long.parseLong(elementAlarmPoint.getAttribute(TIME_02));
                        stringTuneId = elementAlarmPoint.getAttribute(AP_TUNE_ID);
                        _fadeIn = Integer.valueOf(elementAlarmPoint.getAttribute(FADE_IN)) == 1;
                        _protected = Integer.valueOf(elementAlarmPoint.getAttribute(PROTECTED)) == 1;
                        _color = Integer.valueOf(elementAlarmPoint.getAttribute(COLOR)) == 1;
                        _repeat = new ArrayList<Boolean>();

                        Element elementRepeat = (Element) elementAlarmPoint.getElementsByTagName(REPEAT).item(0);

                        for (int j = 0; j < 7; j++) {
                            _repeat.add(Integer.valueOf(elementRepeat.getAttribute("d" + j)) == 1);
                        }
                        _repeatID = Integer.valueOf(elementRepeat.getAttribute(REPEAT_ID));

                    }
                    AlarmPoint ap = new AlarmPoint(new Date(millisDate));
                    if (millisTime01 > 0)
                        ap.setTimePoint(new Time(millisTime01), 1);
                    if (millisTime02 > 0)
                        ap.setTimePoint(new Time(millisTime02), 2);
                    ap.set_tuneId(UUID.fromString(stringTuneId));
                    ap.setRepeat(_repeat);
                    if (ap.isRepeat()) {
                        ap.setRepeatId(_repeatID);
                    }
                    ap.setProperties(_fadeIn, _protected, _color);
                    listFromDataBase.put(ap.getSQLDate(), ap);

                }

            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        if (isRisingPlan) {
            RisingPlan = listFromDataBase;
        } else {
            AlarmPointList = listFromDataBase;
        }

        return listFromDataBase;
    }

    public int saveAlarmPointListToData(Boolean isRisingPlan, HashMap<Date, AlarmPoint> newAlarmPointList) {

        String filename;
        if (isRisingPlan) {
            filename = XML_FILE_RISING_PLAN;
            RisingPlan = newAlarmPointList;
        } else {
            filename = XML_FILE_ALARM_POINT_LIST;
            AlarmPointList = newAlarmPointList;
        }

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/" + FOLDER_NAME);
            myDir.mkdirs();

            File file = new File(myDir, filename);
            if (file.exists()) {
                file.delete();
            }

            Document doc = docBuilder.newDocument();
            Element elementRoot;
            if (isRisingPlan) {
                elementRoot = doc.createElement(RISING_PLAN);
            } else {
                elementRoot = doc.createElement(ALARM_POINTs);
            }
            doc.appendChild(elementRoot);

            for (Map.Entry<Date, AlarmPoint> entry : newAlarmPointList.entrySet()) {
                AlarmPoint ap1 = entry.getValue();
                Element elementAlarmPoint = doc.createElement(ALARM_POINT);
                elementRoot.appendChild(elementAlarmPoint);

                Attr attrDate = doc.createAttribute(DATE);
                attrDate.setValue(String.valueOf(ap1.getSQLDate().getTime()));
                elementAlarmPoint.setAttributeNode(attrDate);

                Attr attrTime01 = doc.createAttribute(TIME_01);
                attrTime01.setValue(
                        String.valueOf((ap1.getSQLTimePoint(1) == null) ? -1 : ap1.getSQLTimePoint(1).getTime()));
                elementAlarmPoint.setAttributeNode(attrTime01);

                Attr attrTime02 = doc.createAttribute(TIME_02);
                attrTime02.setValue(
                        String.valueOf((ap1.getSQLTimePoint(2) == null) ? -1 : ap1.getSQLTimePoint(2).getTime()));
                elementAlarmPoint.setAttributeNode(attrTime02);

                Attr attrTuneId = doc.createAttribute(AP_TUNE_ID);
                attrTuneId.setValue(String.valueOf(ap1.get_tuneId().toString()));
                elementAlarmPoint.setAttributeNode(attrTuneId);

                Attr attrFadeIn = doc.createAttribute(FADE_IN);
                attrFadeIn.setValue(String.valueOf(ap1.isFadeIn() ? 1 : 0));
                elementAlarmPoint.setAttributeNode(attrFadeIn);

                Attr attrProtected = doc.createAttribute(PROTECTED);
                attrProtected.setValue(String.valueOf(ap1.isProtected() ? 1 : 0));
                elementAlarmPoint.setAttributeNode(attrProtected);

                Attr attrColor = doc.createAttribute(COLOR);
                attrColor.setValue(String.valueOf(ap1.isColor() ? 1 : 0));
                elementAlarmPoint.setAttributeNode(attrColor);

                Element elementRepeat = doc.createElement(REPEAT);
                elementAlarmPoint.appendChild(elementRepeat);

                Attr attrRepeatId = doc.createAttribute(REPEAT_ID);
                attrRepeatId.setValue(String.valueOf(ap1.getRepeatId()));
                elementRepeat.setAttributeNode(attrRepeatId);

                List<Boolean> repeatList = ap1.getRepeatList();
                for (int i = 0; i < 7; i++) {
                    Attr attrBool = doc.createAttribute("d" + i);
                    attrBool.setValue(String.valueOf(repeatList.get(i) ? 1 : 0));
                    elementRepeat.setAttributeNode(attrBool);
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        return ReturnCode.OK;
    }

    public int saveAlarmPointToData(Boolean isRisingPlan, AlarmPoint newAlarmPoint) {
        HashMap<Date, AlarmPoint> currentAlarmPointList;
        if (isRisingPlan) {
            currentAlarmPointList = RisingPlan;
        } else {
            currentAlarmPointList = AlarmPointList;
        }

        currentAlarmPointList.put(newAlarmPoint.getSQLDate(), newAlarmPoint);
        return saveAlarmPointListToData(isRisingPlan, currentAlarmPointList);
    }

    public int removeAlarmPointFromData(Boolean isRisingPlan, Date date) {
        HashMap<Date, AlarmPoint> currentAlarmPointList;
        if (isRisingPlan) {
            currentAlarmPointList = RisingPlan;
        } else {
            currentAlarmPointList = AlarmPointList;
        }

        currentAlarmPointList.remove(date);
        return saveAlarmPointListToData(isRisingPlan, currentAlarmPointList);
    }

    public List<Task> GetCurrentToDoListFromData() {
        String filename = XML_FILE_TO_DO_LIST;

        List<Task> currentToDoList = new ArrayList<Task>();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/" + FOLDER_NAME);
            myDir.mkdirs();
            File file = new File(myDir, filename);

            if (!file.exists()) {
                // Initialize
            } else {
                FileInputStream fistream = new FileInputStream(file);
                Document doc = docBuilder.parse(fistream);

                // optional, but recommended
                doc.getDocumentElement().normalize();
                NodeList nToDoList = doc.getElementsByTagName(TASK);

                for (int i = 0; i < nToDoList.getLength(); i++) {
                    Node nodeTask = nToDoList.item(i);
                    int id = -1;
                    String task = "";
                    String time = "";
                    String date = "";

                    // parse data for alarm point
                    if (nodeTask.getNodeType() == Node.ELEMENT_NODE) {
                        Element elementTask = (Element) nodeTask;

                        id = Integer.valueOf(elementTask.getAttribute(TASK_ID));
                        task = elementTask.getAttribute(TASK_NAME);
                        time = elementTask.getAttribute(TASK_TIME);
                        date = elementTask.getAttribute(TASK_DATE);
                    }
                    Task t = new Task(task, date, time);
                    t.setId(id);
                    currentToDoList.add(t);
                }

            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return currentToDoList;
    }

    public int SaveToDoListToData(ArrayList<Task> todoList) {

        String filename = XML_FILE_TO_DO_LIST;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/" + FOLDER_NAME);
            myDir.mkdirs();

            File file = new File(myDir, filename);
            if (file.exists()) {
                file.delete();
            }

            Document doc = docBuilder.newDocument();
            Element elementRoot = doc.createElement(TODO_LIST);
            doc.appendChild(elementRoot);

            for (Task t : todoList) {
                Element elementTask = doc.createElement(TASK);
                elementRoot.appendChild(elementTask);

                Attr attrTaskId = doc.createAttribute(TASK_ID);
                attrTaskId.setValue(String.valueOf(t.getId()));
                elementTask.setAttributeNode(attrTaskId);

                Attr attrTaskName = doc.createAttribute(TASK_NAME);
                attrTaskName.setValue(t.getTask());
                elementTask.setAttributeNode(attrTaskName);

                Attr attrTaskTime = doc.createAttribute(TASK_TIME);
                attrTaskTime.setValue(t.getTime());
                elementTask.setAttributeNode(attrTaskTime);

                Attr attrTaskDate = doc.createAttribute(TASK_DATE);
                attrTaskDate.setValue(t.getDate());
                elementTask.setAttributeNode(attrTaskDate);
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        return ReturnCode.OK;
    }

    public List<Tune> getUserTunesAndDefaultKey() {
        String filename = XML_FILE_USER_TUNES;

        List<Tune> userTunesAndDefaultKey = null;

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/" + FOLDER_NAME);
            myDir.mkdirs();
            File file = new File(myDir, filename);
            userTunesAndDefaultKey = new ArrayList<Tune>();

            if (!file.exists()) {
                // Initialize
                userTunesAndDefaultKey.add(new Tune("null", "null", UUID.fromString(RecommendedTunesHandler.UUID_01)));
                saveUserTunes(userTunesAndDefaultKey);
            } else {
                FileInputStream fistream = new FileInputStream(file);
                Document doc = docBuilder.parse(fistream);

                // optional, but recommended
                doc.getDocumentElement().normalize();
                NodeList nUserTunesList = doc.getElementsByTagName(TUNE);

                for (int i = 0; i < nUserTunesList.getLength(); i++) {
                    Node nodeTune = nUserTunesList.item(i);
                    UUID _keyId = null;
                    String _name = "";
                    String _path = "";

                    // parse data for alarm point
                    if (nodeTune.getNodeType() == Node.ELEMENT_NODE) {

                        Element elementTune = (Element) nodeTune;

                        _keyId = UUID.fromString(elementTune.getAttribute(TUNE_KEY));
                        _name = elementTune.getTextContent();
                        _path = elementTune.getAttribute(TUNE_PATH);

                    }
                    Tune tune = new Tune(_name, _path, _keyId);
                    userTunesAndDefaultKey.add(tune);
                }

            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return userTunesAndDefaultKey;
    }

    private int saveUserTunes(List<Tune> UserTunesAndDefaultKey) {
        String filename = XML_FILE_USER_TUNES;

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/" + FOLDER_NAME);
            myDir.mkdirs();

            File file = new File(myDir, filename);
            if (file.exists()) {
                file.delete();
            }

            Document doc = docBuilder.newDocument();
            Element elementRoot = doc.createElement(TUNES);
            doc.appendChild(elementRoot);

            for (Tune t : UserTunesAndDefaultKey) {
                Element elementTune = doc.createElement(TUNE);
                elementRoot.appendChild(elementTune);

                Attr attrTuneId = doc.createAttribute(TUNE_KEY);
                attrTuneId.setValue(String.valueOf(t.get_keyId()));
                elementTune.setAttributeNode(attrTuneId);

                // NAME
                elementTune.appendChild(doc.createTextNode(t.get_name()));

                Attr attrTunePath = doc.createAttribute(TUNE_PATH);
                attrTunePath.setValue(String.valueOf(t.get_path()));
                elementTune.setAttributeNode(attrTunePath);
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        return ReturnCode.OK;
    }

    public int addTuneToData(Tune newTune) {
        List<Tune> UserTunesAndDefaultKey = getUserTunesAndDefaultKey();
        UserTunesAndDefaultKey.add(newTune);
        saveUserTunes(UserTunesAndDefaultKey);
        return ReturnCode.OK;
    }

    public int changeDefault(UUID default_Id) {
        List<Tune> UserTunesAndDefaultKey = getUserTunesAndDefaultKey();
        UserTunesAndDefaultKey.remove(0);
        UserTunesAndDefaultKey.add(0, new Tune("null", "null", default_Id));
        saveUserTunes(UserTunesAndDefaultKey);
        return ReturnCode.OK;
    }

    public int removeTuneFromData(int position) {
        List<Tune> UserTunesAndDefaultKey = getUserTunesAndDefaultKey();
        UserTunesAndDefaultKey.remove(position + 1); // - default Tune
        saveUserTunes(UserTunesAndDefaultKey);
        return ReturnCode.OK;
    }
}
