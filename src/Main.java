import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class Main {

    static boolean DEBUG_print_dir_info = false;
    static boolean DEBUG_print_thread_info = false;
    static boolean DEBUG_print_file_names = false;

    static boolean Feature_Use_Multi_thread = true;

    static boolean waitForResults = false;
    static long startTime;

    static String[][] data = {};

    static ArrayList<List<String>> allRecords = null;
    private static synchronized void updateAllRecords(ArrayList<List<String>> data) {
        allRecords.addAll(data);
    }

    static int dirCounter = 0;
    private static synchronized int updateDirCounter(int incr) {
        dirCounter = dirCounter + incr;
        return dirCounter;
    }

    static int errorCounter = 0;
    private static synchronized int updateErrorCounter(int incr) {
        errorCounter = errorCounter + incr;
        return errorCounter;
    }

    static int threadCounter = 0;
    private static synchronized int  updateThreadCounter(int incr) {
        threadCounter = threadCounter + incr;
        if (waitForResults && threadCounter == 0)
            displayResults();
        return threadCounter;
    }

    static int fileCounter = 0;
    private static synchronized int updateFileCounter(int incr) {
        fileCounter = fileCounter + incr;
        return fileCounter;
    }

    public static void main(String[] args) {
        allRecords = new ArrayList<List<String>>();
//            List<String> records = readFileByName("c:\\Users\\38095\\Music\\GP Tabs\\Aphorism - Aphorism.gp3");

        // Creates an array in which we will store the names of files and directories
        // String StartPath = "c:\\Users\\38095\\Music\\GP Tabs\\";
        String StartPath = "c:\\Tabs\\";
        StartPath = "c:\\Tabs\\A\\";

        startTime = currentTimeMillis();

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File(StartPath);
        ArrayList<List<String>> dirRecords = readDirectory(f);
        if (dirRecords.size() > 0)
            updateAllRecords(dirRecords);

        if (Feature_Use_Multi_thread)
            waitForResults = true;
        else
            displayResults();
    }

    private static void displayResults()
    {
        long endTime = currentTimeMillis();
        StringBuilder status = new StringBuilder("*** FINISH ***");

        System.out.println("*** FINISH ***");
        System.out.println("*** number of   dirs: " + dirCounter);
        status.append("*** number of   dirs: ");
        status.append(dirCounter);

        System.out.println("*** number of  files: " + fileCounter);
        status.append("*** number of files: ");
        status.append(fileCounter);

        System.out.println("*** number of errors: " + errorCounter);
        status.append("*** number of errors: ");
        status.append(errorCounter);

        System.out.println("*** all records : " + allRecords.size());
        System.out.println("*** all good files : " + (fileCounter - errorCounter));
        status.append("*** number records: ");
        status.append(allRecords.size());

        System.out.println("*** processing time, sec: " + ((endTime - startTime)/1000.0));
        status.append("*** processing time, sec: ");
        status.append(((endTime - startTime)/1000.0));

        // convert allRecords into grid format
        data = new String[allRecords.size()] [9];
        int i=0;
        for (List<String> record: allRecords)
        {
            data[i] = record.toArray(new String[9]);
            i++;
        }
        // Creating instance of JFrame
        JFrame frame = new JFrame("My First Swing Example");
        // Setting the width and height of frame
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        placeComponents(frame);


        // create the status bar panel and shove it down the bottom of the frame
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        frame.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel statusLabel = new JLabel(status.toString());
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);

        frame.setVisible(true);
    }

    private static void placeComponents(JFrame panel) {
        String[] column ={"version","title","subtitle","artist","album","words","copyright","tabbed by","instructions"};
        JTable jt = new JTable(data,column);
        jt.setBounds(30,40,200,300);
        JScrollPane sp=new JScrollPane(jt);
        panel.add(sp);
/*
        panel.setLayout(null);

        // Creating JLabel
        JLabel userLabel = new JLabel("User");

        userLabel.setBounds(10,20,80,25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100,20,165,25);
        panel.add(userText);

        // Same process for password label and text field.
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10,50,80,25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100,50,165,25);
        panel.add(passwordText);

        // Creating login button
        JButton loginButton = new JButton("login");
        loginButton.setBounds(10, 80, 80, 25);
        panel.add(loginButton); */
    }

    private static ArrayList<List<String>> readDirectory(File filename) {
        // Populates the array with names of files and directories
        File[] pathNames = filename.listFiles();
        updateDirCounter(1);
        if (DEBUG_print_dir_info)
            System.out.println("--- " + filename.getName());

        if (pathNames == null) return null;

        ArrayList<List<String>> newRecords = new ArrayList<List<String>>();
        // For each pathname in the pathNames array
        for (File pathname : pathNames) {
            if (pathname.isFile()) {
                try {
                    List<String> records = readFileByName(pathname.getPath());
                    newRecords.add(records);
                    if (DEBUG_print_file_names)
                        System.out.println("reading "+ fileCounter + " : " + pathname.getName());
                }
                catch (Exception e) {
                    updateErrorCounter(1);
                    System.out.println("Error: " + e + " - at reading file: " + pathname.getName());
                }
            }
            else if (pathname.isDirectory())
            {
                if (Feature_Use_Multi_thread) {
                    Thread thread = new Thread() {
                        public void run() {
                            updateThreadCounter(1);
                            if (DEBUG_print_thread_info)
                                System.out.println("Thread " + threadCounter + " Running: " + pathname);
                            ArrayList<List<String>> dirRecords = readDirectory(pathname);
                            if (dirRecords.size() > 0)
                            {
                                updateAllRecords(dirRecords);
                            }
                            if (DEBUG_print_thread_info)
                                System.out.println("Thread " + threadCounter + " STOP: " + pathname + "--- new size: " + allRecords.size());
                            updateThreadCounter(-1);
                        }
                    };
                    thread.start();
                }
                else
                {
                    ArrayList<List<String>> dirRecords = readDirectory(pathname);
                    if (dirRecords.size() > 0)
                        allRecords.addAll(dirRecords);
                }
            }
        }

        return newRecords;
    }

    private static List<String> readFileByName(String filename) throws Exception {
        updateFileCounter(1);
        List<String> records = new ArrayList<String>();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(filename, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (raf == null)
            throw new Exception("Error 3: Error open file: " + filename);
        String version = readVersion(raf);

        if (version.equals("")) {
            throw new Exception("Error 1: Wrong file version");
        }
        records.add(version);

        do {
            records.add(readString(raf));
        } while (records.size() < 9);
        raf.close();
        return records;
    }

    private static String readVersion(RandomAccessFile raf) throws Exception {
        int versionLength = raf.read();
        if ((versionLength == 0) || (versionLength > 30))
            throw new Exception("Error 5: Wrong file version size: '" + versionLength + "'");

        byte[] charBuffer = new byte[30];
        raf.read(charBuffer, 0, (30));

        String str = "";
        for (int i=0; i<versionLength; i++)
            str += (char) (charBuffer[i]);

        if ((!str.contains("FICHIER GUITAR PRO v3")) &&
            (!str.contains("FICHIER GUITAR PRO v4")) &&
            (!str.contains("FICHIER GUITAR PRO v5")))
            throw new Exception("Error 4: Wrong file version: '" + str + "'");

        return str;
    }

    private static String readString(RandomAccessFile raf) throws Exception {
        int ch1 = raf.read();
        int len = raf.readInt();

        if (len > 255)
            throw new Exception("Error 2: Error reading String");

        byte[] charBuffer = new byte[len];
        raf.read(charBuffer, 0, (len));

        String str = "";
        for (int i=0; i<len; i++)
            str += (char) (charBuffer[i]);
        return str;
    }
}
/*
*** FINISH ***
        *** number of   dirs: 10380
        *** number of  files: 98831
        *** number of errors: 3567
        *** processing time, sec: 237.982
 *** all records : 95264
 *** all good files : 95264
*/