package ticketfan.service;

import ticketfan.model.TicketPrintData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.file.Path;

/** Genera el boleto del cliente en formato PDF. */
public class PdfTicketService {

    /**
     * Genera un archivo PDF con los datos de una compra y su boleto.
     *
     * @param t datos del boleto que se imprimirá
     * @param destino ruta donde se guardará el archivo PDF
     * @throws IOException si ocurre un error al crear o guardar el documento
     */
    public void crearPdf(TicketPrintData t, Path destino) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setLeading(22f);
                cs.newLineAtOffset(70, 700);
                cs.setFont(PDType1Font.HELVETICA_BOLD, 22);
                cs.showText("TicketFan - Boleto");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 13);
                cs.showText("Compra: " + t.idCompra());
                cs.newLine();
                cs.showText("Cliente: " + limpiar(t.cliente()));
                cs.newLine();
                cs.showText("Evento: " + limpiar(t.evento()));
                cs.newLine();
                cs.showText("Folio: " + limpiar(t.folio()));
                cs.newLine();
                cs.showText("Codigo: " + limpiar(t.codigoQr()));
                cs.newLine();
                cs.showText("Fecha de compra: " + t.fechaCompra());
                cs.newLine();
                cs.showText("Precio: $" + t.precio());
                cs.newLine();
                cs.showText("Estado: " + limpiar(t.estado()));
                cs.endText();
            }
            doc.save(destino.toFile());
        }
    }

    private String limpiar(String s) {
        if (s == null) return "";
        return s.replaceAll("[^\\x20-\\x7EáéíóúÁÉÍÓÚñÑüÜ]", "");
    }
}
