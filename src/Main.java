import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class Main {

    static boolean DEBUG_print_dir_names = false;
    static boolean DEBUG_print_file_names = false;

    static ArrayList<List<String>> allRecords = null;
    static String[][] data ={};
    static int fileCounter = 0;
    static int dirCounter = 0;
    static int errorCounter = 0;

    public static void main() {
        // Creating instance of JFrame
        JFrame frame = new JFrame("My First Swing Example");
        // Setting the width and height of frame
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        long startTime = currentTimeMillis();

        allRecords = new ArrayList<List<String>>();
//            List<String> records = readFileByName("c:\\Users\\38095\\Music\\GP Tabs\\Aphorism - Aphorism.gp3");

        // Creates an array in which we will store the names of files and directories
        // String StartPath = "c:\\Users\\38095\\Music\\GP Tabs\\";
        // String StartPath = "c:\\Tabs\\A\\";
        String StartPath = "c:\\Tabs\\";

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File(StartPath);
        readDirectory(f);
        data = new String[allRecords.size()] [9];
        int i=0;
        for (List<String> record: allRecords)
        {
            data[i] = record.toArray(new String[9]);
            i++;
        }
        System.out.println("*** FINISH ***");
        System.out.println("*** number of   dirs: " + dirCounter);
        System.out.println("*** number of  files: " + fileCounter);
        System.out.println("*** number of errors: " + errorCounter);

        long endTime = currentTimeMillis();
        System.out.println("*** processing time, sec: " + ((endTime - startTime)/1000.0));

        placeComponents(frame);
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

    private static void readDirectory(File filename) {
        // Populates the array with names of files and directories
        File[] pathNames = filename.listFiles();
        dirCounter++;
        if (DEBUG_print_dir_names)
            System.out.println("*** " + filename.getName());

        if (pathNames == null) return;

        // For each pathname in the pathNames array
        for (File pathname : pathNames) {
            if (pathname.isFile()) {
                try {
                    List<String> records = readFileByName(pathname.getPath());
                    allRecords.add(records);
                    if (DEBUG_print_file_names)
                        System.out.println("reading "+ fileCounter + " : " + pathname.getName());
                }
                catch (Exception e) {
                    errorCounter++;
                    System.out.println("Error: " + e + " - at reading file: " + pathname.getName());
                }
            }
            else if (pathname.isDirectory())
            {
                readDirectory(pathname);
            }
        }
    }


    private static List<String> readFileByName(String filename) throws Exception {
        fileCounter++;
        List<String> records = new ArrayList<String>();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(filename, "rw");
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

        return records;
    }

    private static String readVersion(RandomAccessFile raf) throws IOException {
        int versionLength = raf.read();
        if ((versionLength == 0) || (versionLength > 30))
            return "";

        byte[] charBuffer = new byte[30];
        raf.read(charBuffer, 0, (30));

        String str = "";
        for (int i=0; i<versionLength; i++)
            str += (char) (charBuffer[i]);

        if ((!str.contains("FICHIER GUITAR PRO v3")) &&
            (!str.contains("FICHIER GUITAR PRO v4")) &&
            (!str.contains("FICHIER GUITAR PRO v5")))
            return "";

        return str;
    }

    private static String readString(RandomAccessFile raf) throws Exception {
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