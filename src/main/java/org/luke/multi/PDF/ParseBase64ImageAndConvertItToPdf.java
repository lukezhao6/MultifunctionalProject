

import com.google.common.collect.Lists;
import com.itextpdf.barcodes.Barcode39;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.HorizontalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class ParseBase64ImageAndConvertItToPdf {

    public void parseBase64ImageAndConvertItToPdf(HttpServletResponse response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);
        int errorCount = 0;
        try {
            String resultStr = callApi("paramsStr");
            if (StringUtils.isNotBlank(resultStr)) {
                org.jsoup.nodes.Document document = Jsoup.parse(resultStr);
                Elements elements = document.select("img");
                Element element1 = document.selectFirst("img");
                List<byte[]> imageBytesList = Lists.newArrayList();
                for (Element element : elements) {
                    String base64Image = element.attr("src").split(",")[1];
                    byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64Image);
                    imageBytesList.add(imageBytes);
                }
                if (!imageBytesList.isEmpty()) {
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytesList.get(0)));
                    float width = bufferedImage.getWidth();
                    float height = bufferedImage.getHeight();
                    float widthInPoints = width * 72 / 96;
                    float heightInPoints = height * 72 / 96;
                    PageSize pageSize = new PageSize(widthInPoints, heightInPoints);
                    pdfDoc.addNewPage(pageSize);
                    Barcode39 barcode = new Barcode39(pdfDoc);
                    barcode.setCode("123456");
                    Image barcodeImage = new Image(barcode.createFormXObject(null, null, pdfDoc));
                    barcodeImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    barcodeImage.setMarginTop((heightInPoints - heightInPoints / 8) / 2);
                    if (width <= height) {
                        barcodeImage.setWidth(350);
                        barcodeImage.setHeight(100);
                    } else {
                        barcodeImage.setWidth(600);
                    }
                    doc.add(barcodeImage);

                    for (byte[] imageBytes : imageBytesList) {
                        pdfDoc.addNewPage(pageSize);
                        doc.setMargins(0, 0, 0, 0);
                        Image img = new Image(ImageDataFactory.create(imageBytes));
                        img.setHorizontalAlignment(HorizontalAlignment.CENTER);
                        doc.add(img);
                    }
                }
            } else {
                errorCount++;
            }
        } catch (Exception e) {
            errorCount++;
        } finally {
            if (errorCount > 0) {
                for (int i = 0; i < errorCount; i++) {
                    String externalPdfUrl = "https://test.luke.com/fail.pdf";
                    PdfDocument externalPdfDoc = new PdfDocument(new PdfReader(externalPdfUrl));
                    PdfPage page = externalPdfDoc.getPage(1);
                    pdfDoc.addPage(page.copyTo(pdfDoc));
                    externalPdfDoc.close();
                }
            }
            doc.close();
            writer.close();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"output.pdf\"");
            response.setContentLength(baos.size());
            baos.writeTo(response.getOutputStream());
        }
    }

    private String callApi(String paramsStr) {
        return null;
    }
}
