package com.kmaismith.chunkclaim.Data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public interface IData {

    final static String dataLayerFolderPath = "plugins" + File.separator + "ChunkClaim";

    abstract void writeDataToFile(BufferedWriter outStream) throws IOException;

    abstract void readDataFromFile(BufferedReader inStream) throws IOException;

    abstract File getFile();
}
