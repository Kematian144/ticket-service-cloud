package cl.duoc.cloud.ticketservice.service;

import cl.duoc.cloud.ticketservice.model.Ticket;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfTicketGeneratorService {

    // Genera el documento PDF del ticket de forma sencilla
    public byte[] generateTicketPdf(Ticket ticket) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(20, 50, 90));
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

        Paragraph title = new Paragraph("TICKET DE ACCESO - SISTEMA DE EVENTOS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15);
        document.add(title);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addRow(table, "Ticket ID:", ticket.getTicketId(), labelFont, valueFont);
        addRow(table, "Evento:", ticket.getEventName() + " (ID: " + ticket.getEventId() + ")", labelFont, valueFont);
        addRow(table, "Usuario:", ticket.getUserName() + " (" + ticket.getUserId() + ")", labelFont, valueFont);
        addRow(table, "Email:", ticket.getUserEmail() != null ? ticket.getUserEmail() : "N/A", labelFont, valueFont);
        addRow(table, "Asiento:", ticket.getSeatNumber(), labelFont, valueFont);
        addRow(table, "Precio:", "$" + ticket.getPrice() + " CLP", labelFont, valueFont);
        addRow(table, "Estado:", ticket.getStatus().name(), labelFont, valueFont);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dateStr = ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(fmt) : "Reciente";
        addRow(table, "Fecha Emision:", dateStr, labelFont, valueFont);

        document.add(table);

        Paragraph footer = new Paragraph("\nTicket digital generado en AWS EFS y respaldado en AWS S3.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setPadding(5);
        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setPadding(5);
        table.addCell(c1);
        table.addCell(c2);
    }
}
