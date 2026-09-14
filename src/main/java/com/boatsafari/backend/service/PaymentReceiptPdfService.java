package com.boatsafari.backend.service;

import com.boatsafari.backend.dto.PaymentReceipt;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PaymentReceiptPdfService {

    public byte[] generateReceiptPdf(PaymentReceipt receipt) {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDFont titleFont =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD);

            PDFont textFont =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream contentStream =
                         new PDPageContentStream(document, page)) {

                // Title
                contentStream.beginText();
                contentStream.setFont(titleFont, 18);
                contentStream.newLineAtOffset(160, 780);
                contentStream.showText(
                        "Boat Safari Payment Receipt");
                contentStream.endText();

                float y = 730;

                y = writeLine(
                        contentStream,
                        textFont,
                        "Receipt Number",
                        receipt.getReceiptNumber(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Payment ID",
                        receipt.getPaymentId(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Booking ID",
                        receipt.getBookingId(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Customer Name",
                        receipt.getCustomerName(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Customer Email",
                        receipt.getCustomerEmail(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Number of Passengers",
                        receipt.getNumberOfPassengers(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Trip Date",
                        receipt.getTripDate(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Start Time",
                        receipt.getStartTime(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "End Time",
                        receipt.getEndTime(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Boat Name",
                        receipt.getBoatName(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Amount",
                        "LKR " + receipt.getAmount(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Payment Method",
                        receipt.getPaymentMethod(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Payment Status",
                        receipt.getPaymentStatus(),
                        y);

                y = writeLine(
                        contentStream,
                        textFont,
                        "Transaction Reference",
                        receipt.getTransactionReference(),
                        y);

                writeLine(
                        contentStream,
                        textFont,
                        "Payment Date",
                        receipt.getPaymentDate(),
                        y);
            }

            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not generate receipt PDF",
                    e);
        }
    }

    private float writeLine(
            PDPageContentStream contentStream,
            PDFont font,
            String label,
            Object value,
            float y) throws IOException {

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(70, y);

        contentStream.showText(
                label + ": " + String.valueOf(value));

        contentStream.endText();

        return y - 28;
    }
}