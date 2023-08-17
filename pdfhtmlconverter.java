package main.java;

import com.groupdocs.conversion.Converter;
import com.groupdocs.conversion.options.convert.MarkupConvertOptions;
import com.groupdocs.viewer.Viewer;
import com.groupdocs.viewer.options.HtmlViewOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class pdfhtmlconverter {

    public static final String CONVERTED_DIR_NAME = "_CONVERTED_FILES";

    public static void main(String[] args) {


        try {/*Converter converter = new Converter("C:\\Users\\Public\\Downloads\\qc.pdf");
        MarkupConvertOptions mk = new MarkupConvertOptions();
        int count = converter.getDocumentInfo().getPagesCount();
        mk.setPages(Arrays.asList(1, 2));

        mk.setFixedLayout(true);
        mk.setFixedLayoutShowBorders(false);
        mk.setZoom(150);
        //converter.convert("C:\\Users\\Public\\Downloads\\qc" + 1 + ".html", mk);*/
            if (args.length == 0) {
                args = new String[]{"C:\\Users\\Public\\Downloads\\aws.pdf"};
                System.out.println("Please pass filename");
            }

            String fileName = args[0];


            String convertedFilesDir = getConvertedFilesDirectory(fileName);
            String embeddedResourcesDir = convertedFilesDir + "\\EmbeddedResources_Files";
            boolean isCreated =  new File(embeddedResourcesDir).mkdirs();

            Converter converter = new Converter(fileName);
            int count = converter.getDocumentInfo().getPagesCount();
            MarkupConvertOptions mk = new MarkupConvertOptions();
            mk.setFixedLayout(true);
            mk.setFixedLayoutShowBorders(false);
            mk.setZoom(150);
            int pagePadCount = String.valueOf(count).length();
            for (int i = 1; i <= count; i++) {
                mk.setPages(Arrays.asList(i));
                converter = new Converter(fileName);
                String pageName = "Page" + StringUtils.leftPad(String.valueOf(i), pagePadCount, "0") + ".xhtml";
                converter.convert(embeddedResourcesDir + "\\" + pageName, mk);
            }
            converter.close();
            SplitPDFFile(fileName);
            ConvertToEPub(fileName);
            //generateHTMLFromPDF("C:\\Users\\Public\\Downloads\\qc.pdf");

        }catch (Exception ex){
            System.out.println(ex.getMessage());

        }

    }

    private static  String getFileNameWithoutExtension(String fileName){
        Path p = Paths.get(fileName);
        String fileNameWithoutExtension = p.getFileName().toString().replaceAll("\\.\\w+", "");
        return fileNameWithoutExtension;
    }

    private  static String getConvertedFilesDirectory(String fileName){
        Path p = Paths.get(fileName);
        Path parentFolder = p.getParent();
        String fileNameWithoutExtension = p.getFileName().toString().replaceAll("\\.\\w+", "");
        String dirName = parentFolder.toAbsolutePath().toString() + "\\" + fileNameWithoutExtension + CONVERTED_DIR_NAME;
        boolean isCreated =  new File(dirName).mkdirs();
        return dirName;
    }


    private static  void SplitPDFFile(String fileName) throws Exception{

        String fileNameWithoutExtension = getFileNameWithoutExtension(fileName);
        String convertedFileDir = getConvertedFilesDirectory(fileName);
        String splitFileDir = convertedFileDir + "\\Split_Files";
        boolean isCreated =  new File(splitFileDir).mkdirs();


        // Loading PDF
        File pdffile
                = new File(fileName);
        PDDocument document = PDDocument.load(pdffile);

        // Splitter Class
        Splitter splitting = new Splitter();

        // Splitting the pages into multiple PDFs
        List<PDDocument> Page = splitting.split(document);


        // Using a iterator to Traverse all pages
        Iterator<PDDocument> iteration
                = Page.listIterator();

        String totalPageCount = String.valueOf(document.getNumberOfPages());

        // Saving each page as an individual document
        int j = 1;
        while (iteration.hasNext()) {
            PDDocument pd = iteration.next();
            String pageName = "Page" + StringUtils.leftPad(String.valueOf(j), totalPageCount.length(), "0") + ".pdf";
            pd.save(splitFileDir + "\\" + pageName);
            pd.close();
            j++;
        }

        document.close();
    }


    private  static void CreateHtmlCssAndFontsDirectories(String fileName){
        String convertedFileDir = getConvertedFilesDirectory(fileName);
        String[] fileTypes = new String[]{"Html", "Resources"};

        for(String fileType: fileTypes){
            String dir = convertedFileDir + "\\" + fileType;
            boolean isCreated =  new File(dir).mkdirs();
        }
    }

    private static void ConvertToEPub(String fileName){

        Path p = Paths.get(fileName);
        Path folder = p.getParent();
        String fileNameWithoutExtension = p.getFileName().toString().replaceAll("\\.\\w+", "");
        String convertedFileDir = getConvertedFilesDirectory(fileName);
        String htmlDir = convertedFileDir + "\\Html";
        String resourcesDir = convertedFileDir + "\\Resources";

        String splitDirName = convertedFileDir + "\\" +  "Split_Files";

        CreateHtmlCssAndFontsDirectories(fileName);

        File splitFolder = new File(splitDirName);
        File[] listOfFiles = splitFolder.listFiles();
        int j = 1;
        for (File splitFile : listOfFiles) {
            System.out.println("working on file " + j);
            if (splitFile.isFile()) {
                try (Viewer viewer = new Viewer(splitFile.getAbsolutePath())) {
                    // Create an HTML files.
                    // {0} is replaced with the current page number in the file name.
                    System.out.println("working on file inside" + j);
                    String fielNameWithoutExtension = getFileNameWithoutExtension(splitFile.getAbsolutePath());
                    HtmlViewOptions viewOptions = HtmlViewOptions.forExternalResources(htmlDir + "\\" + fielNameWithoutExtension + ".xhtml", resourcesDir + "\\"+ fielNameWithoutExtension +"_{1}", resourcesDir +"\\" + fielNameWithoutExtension + "_{1}");
                    viewer.view(viewOptions);
                    j++;
                }
            }
        }
    }



}
