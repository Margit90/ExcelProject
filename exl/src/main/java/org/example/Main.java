package org.example;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.sql.SQLOutput;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {

    static String exeToPut;
    static String exelFile;

    public static void main(String[] args) throws Exception {

        getPaths();
        addExe();
        addcontentypes();

    }

    private static void getPaths() {
        System.out.println("give me filepath of .exe");
        Scanner keyboard = new Scanner(System.in);
        exeToPut = keyboard.nextLine().replace("\"", "");
        System.out.println("give me filepath of .exelsheet");
        exelFile = keyboard.nextLine().replace("\"", "");

    }

    private static void addcontentypes() throws URISyntaxException, IOException {


        String oldContent = readZipFile(exelFile);

        if(oldContent == null) {

            String xml = "Content_Types.xml";
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");

            String file = readFromInputStream(Main.class.getClassLoader().getResourceAsStream(xml));

            Path path = Path.of(exelFile);
            URI uri = URI.create("jar:" + path.toUri());

            try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
                String newFilename = "[Content_Types].xml";
                Path nf = fs.getPath(newFilename);


                Files.write(nf, file.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {

            String inputExe = "<Default Extension=\"exe\" ContentType=\"application/vnd.microsoft.portable-executable\" />";

            if (oldContent.contains(inputExe)){

                System.out.println("ContentType exe Eintrag schon vorhanden");
            }
            else {
                //System.out.println("find Index");

                int index = oldContent.indexOf("<Default");

                String newContent = oldContent.substring(0,index)+inputExe+oldContent.substring(index);

                //System.out.println("New Content"+newContent);
                //System.out.println("Old Content"+oldContent);

                Path path = Path.of(exelFile);
                URI uri = URI.create("jar:" + path.toUri());

                try (FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<>())) {
                    String newFilename = "[Content_Types].xml";
                    Path nf = fs.getPath(newFilename);


                    Files.write(nf, newContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }

    }

    private static String readZipFile(String zipSource) {

        try(ZipFile zipFile = new ZipFile(new File(zipSource))) {

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String filename = zipEntry.getName();
                if (filename.contains("[Content_Types]") && filename.endsWith("xml")) {
                    //System.out.println(zipEntry.getName());
                    String text = new String(zipFile.getInputStream(zipEntry).readAllBytes(), StandardCharsets.UTF_8);
                    return text;


                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    private static void addExe() throws IOException {


        String file3 = exeToPut;
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        Path path1 = Paths.get(file3);


        Path path = Path.of(exelFile);
        URI uri = URI.create("jar:" + path.toUri());

        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            String newFilename = file3.replaceAll("^.*[\\/\\\\]", "");
            Path nf = fs.getPath(newFilename);


            Files.write(nf, Files.readAllBytes(path1), StandardOpenOption.CREATE);
        }
    }
}