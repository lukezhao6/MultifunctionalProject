
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
        // 创建一个ByteArrayOutputStream对象，用于将生成的PDF内容写入内存中的字节数组。
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 使用PdfWriter将字节输出流连接到PDF文档。
        PdfWriter writer = new PdfWriter(baos);
        // 创建PdfDocument对象，这是PDF文档的主要表示形式。
        PdfDocument pdfDoc = new PdfDocument(writer);
        // 创建Document对象，用于在PDF文档中添加内容。
        Document doc = new Document(pdfDoc);
        // 初始化一个错误计数器errorCount，用于记录处理过程中出现的错误。
        int errorCount = 0;
        try {
            // 调用callApi方法获取一个包含Base64编码图像的字符串resultStr。
            String resultStr = callApi("paramsStr");
            if (StringUtils.isNotBlank(resultStr)) {
                // 使用Jsoup解析resultStr，将其转换为一个HTML文档对象。
                org.jsoup.nodes.Document document = Jsoup.parse(resultStr);
                // 选择所有<img>元素。
                Elements elements = document.select("img");
                // 选择第一个<img>元素。
                Element element1 = document.selectFirst("img");
                // 初始化一个列表imageBytesList，用于存储解析后的图像字节数据。
                List<byte[]> imageBytesList = Lists.newArrayList();
                for (Element element : elements) {
                    // 从每个<img>元素的src属性中提取Base64编码的图像数据。
                    String base64Image = element.attr("src").split(",")[1];
                    // 将Base64编码的图像数据解码为字节数组，并添加到imageBytesList中。
                    byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64Image);
                    imageBytesList.add(imageBytes);
                }
                if (!imageBytesList.isEmpty()) {
                    // 使用ImageIO读取字节数组中的图像并获取其宽度和高度。
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytesList.get(0)));
                    float width = bufferedImage.getWidth();
                    float height = bufferedImage.getHeight();
                    // 将像素转换为PDF的点（point）单位。
                    float widthInPoints = width * 72 / 96;
                    float heightInPoints = height * 72 / 96;
                    // 创建一个新的页面大小PageSize。
                    PageSize pageSize = new PageSize(widthInPoints, heightInPoints);
                    // 向PDF文档添加一个新页面，使用上述页面大小。
                    pdfDoc.addNewPage(pageSize);
                    // 创建一个条形码对象并设置条形码数据。
                    Barcode39 barcode = new Barcode39(pdfDoc);
                    barcode.setCode("123456");
                    // 将条形码转换为图像并设置其位置和大小，然后添加到PDF文档中。
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
                        // 遍历图像字节列表，为每张图像添加一个新的页面。
                        pdfDoc.addNewPage(pageSize);
                        // 设置页面边距为0，并将图像添加到页面中。
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
            // 如果错误计数器大于0，表示处理过程中发生了错误。
            if (errorCount > 0) {
                for (int i = 0; i < errorCount; i++) {
                    // 从指定的URL加载一个外部PDF文档，并将其第一页复制到当前PDF文档中。
                    String externalPdfUrl = "https://test.luke.com/fail.pdf";
                    PdfDocument externalPdfDoc = new PdfDocument(new PdfReader(externalPdfUrl));
                    PdfPage page = externalPdfDoc.getPage(1);
                    pdfDoc.addPage(page.copyTo(pdfDoc));
                    externalPdfDoc.close();
                }
            }
            // 关闭文档和写入器，释放资源。
            doc.close();
            writer.close();
            // 设置HTTP响应的内容类型为PDF，并指定文件名。
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"output.pdf\"");
            // 设置响应内容长度，并将字节数组输出流中的数据写入响应输出流。
            response.setContentLength(baos.size());
            baos.writeTo(response.getOutputStream());
        }
    }

    private String callApi(String paramsStr) {
        return null;
    }
}
