package com.yuuki.autoupdater.main;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * This class will be used to auto-update any application.
 *
 * It works reading a xml file on a remote host (UPDATE_URL) with the following structure:
 * <programName>
 *     <version>@version</version>
 *     <changelog>@changelog</changelog>
 *     <file-name>@file-name</file-name>
 *     <download-link>@download-link</download-link>
 * </programName>
 *
 * If you want to add another tag remember to add the correspondent 'reader' into parseData method.
 *
 * Once the AutoUpdater object has been instanced the updateApplication method will automatically update the application.
 *
 * @author Yuuki
 * @date 09/08/2015 | 1:01
 * @package com.yuuki.aurora.main
 */
public class AutoUpdater {
    //Url where updateXML is located by default
    private static final String UPDATE_URL = "http://borjadev.me/aurora/update.xml";
    //Root directory where the update will be downloaded | Main extracted
    private final String ROOT              = "temp/";
    private final String MAIN_DIRECTORY    = FilenameUtils.getFullPathNoEndSeparator(new File(ROOT).getAbsolutePath());

    //Actual version of the program
    private String actualVersion;
    //Update xml with all the update information
    private Document updateXML;
    //Used to store the different programs in the xml
    private NodeList xmlNodes;


    //Data of the xml file
    private String updateVersion;
    private String changelog;
    private String fileName;
    private String downloadLink;

    /**
     * If you want to use the default url of above.
     * @param programName Name of the program
     * @param actualVersion Actual version of the program, to check if a update is needed
     * @throws Exception
     */
    public AutoUpdater(String programName, String actualVersion) throws Exception {
        this(programName, actualVersion, UPDATE_URL);
    }

    /**
     * In case that you want to use a different url
     * @param actualVersion Actual version of the program, to check if a update is needed
     * @param updateXMLURL New update xml url
     * @throws Exception
     */
    public AutoUpdater(String programName, String actualVersion, String updateXMLURL) throws Exception {
        this.actualVersion = actualVersion;
        this.updateXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(updateXMLURL);

        this.parseData(programName);
    }

    /**
     * Gets the information of the different tags in the xml file.
     * @param programName 'Main-tag' to start reading
     */
    private void parseData(String programName) {
        //See http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        this.updateXML.getDocumentElement().normalize();
        //gets the nodes of the current program
        this.xmlNodes = this.updateXML.getElementsByTagName(programName);

        //foreach 'tag' into the programName tag
        for (int temp = 0; temp < xmlNodes.getLength(); temp++) {
            Node nNode = xmlNodes.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                updateVersion   = eElement.getElementsByTagName("version").item(0).getTextContent();
                changelog       = eElement.getElementsByTagName("changelog").item(0).getTextContent();
                fileName        = eElement.getElementsByTagName("file-name").item(0).getTextContent();
                downloadLink    = eElement.getElementsByTagName("download-link").item(0).getTextContent();
            }
        }
    }

    /**
     * Check if the current version of the program match the one on the xml
     * @return True/false
     */
    public boolean updateAvailable() {
        return !actualVersion.equals(updateVersion);
    }

    /**
     * Updates the application.
     * @throws IOException
     */
    public void updateApplication() throws IOException {
        if(updateAvailable()) {
            downloadFiles();
            //Only if the file is .zip
            unzip();
            cleanUp();
        }
    }

    /**
     * Download the update file (generally .zip)
     * @throws IOException If can't connect to the server or any kind of I/O problem while downloading
     */
    private void downloadFiles() throws IOException {
        URL source = new URL(getDownloadLink());
        File destination = new File(ROOT + getFileName());

        FileUtils.copyURLToFile(source, destination);
    }

    /**
     * If the downloaded file is .zip this method will uncompress it into the main directory
     */
    private void unzip() {
        //check if the file is a zip file
        if(getFileName().split("\\.")[1].equals("zip")) {
            try {
                ZipFile zipFile = new ZipFile(ROOT + getFileName());
                //This will extrat the zip file out of the temp folder
                zipFile.extractAll(MAIN_DIRECTORY);
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete all the temporary files and directories
     * @throws IOException
     */
    private void cleanUp() throws IOException {
        FileUtils.deleteDirectory(new File(ROOT));
    }

    /**
     * GETTERS
     */

    public String getUpdateVersion() {
        return updateVersion;
    }

    public String getChangelog() {
        return changelog;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadLink() {
        return downloadLink;
    }
}
