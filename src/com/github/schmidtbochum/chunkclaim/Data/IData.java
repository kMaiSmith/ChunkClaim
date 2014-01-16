package com.github.schmidtbochum.chunkclaim.Data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: kyle
 * Date: 1/15/14
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IData {

    final static String dataLayerFolderPath = "plugins" + File.separator + "ChunkClaim";

    abstract void writeDataToFile(BufferedWriter outStream) throws IOException;

    abstract void readDataFromFile(BufferedReader inStream) throws IOException;

    abstract File getFile();
}
