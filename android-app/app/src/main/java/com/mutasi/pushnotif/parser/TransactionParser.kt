package com.mutasi.pushnotif.parser

import java.util.regex.Pattern

data class TransactionInfo(
    val bankName: String,
    val transactionType: String, // "credit" or "debit"
    val amount: Double,
    val accountNumber: String?,
    val senderName: String?,
    val timestamp: String?,
    val isQRIS: Boolean = false,
    val rawText: String
)

object TransactionParser {
    
    private val BCA_PATTERN = Pattern.compile(
        "(?:Mutasi rekening|Transfer).*?Rp\\s*([\\d,.]+).*?(?:dari|ke)\\s*([\\d-]+)?",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )
    
    private val MANDIRI_PATTERN = Pattern.compile(
        "(?:Transfer|Terima).*?Rp\\s*([\\d,.]+).*?(?:dari|ke|rek)\\s*([\\d-]+)?",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )
    
    private val BRI_PATTERN = Pattern.compile(
        "(?:Transfer|Penerimaan).*?Rp\\s*([\\d,.]+).*?(?:dari|ke)\\s*([\\d-]+)?",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )
    
    private val BNI_PATTERN = Pattern.compile(
        "(?:Transfer|Terima).*?Rp\\s*([\\d,.]+).*?(?:dari|ke|rek)\\s*([\\d-]+)?",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )
    
    private val AMOUNT_PATTERN = Pattern.compile(
        "Rp\\s*([\\d,.]+)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val ACCOUNT_PATTERN = Pattern.compile(
        "(?:rek|rekening|dari|ke|no\\.?)\\s*[:.]?\\s*([\\d-]{6,})",
        Pattern.CASE_INSENSITIVE
    )
    
    fun parseTransaction(packageName: String, title: String, body: String, bigText: String): TransactionInfo? {
        val fullText = "$title $body $bigText"
        
        // Detect bank from package name
        val bankName = when {
            packageName.contains("bca", ignoreCase = true) -> "BCA"
            packageName.contains("mandiri", ignoreCase = true) -> "Mandiri"
            packageName.contains("bri", ignoreCase = true) -> "BRI"
            packageName.contains("bni", ignoreCase = true) -> "BNI"
            packageName.contains("jago", ignoreCase = true) -> "Jago"
            packageName.contains("seabank", ignoreCase = true) -> "Seabank"
            packageName.contains("dana", ignoreCase = true) -> "DANA"
            packageName.contains("ovo", ignoreCase = true) -> "OVO"
            packageName.contains("gopay", ignoreCase = true) -> "GoPay"
            else -> "Unknown"
        }
        
        // Check if it's a transaction notification
        if (!isTransactionNotification(fullText)) {
            return null
        }
        
        // Detect QRIS
        val isQRIS = fullText.contains("QRIS", ignoreCase = true) || 
                     fullText.contains("QR", ignoreCase = true) && fullText.contains("bayar", ignoreCase = true)
        
        // Parse amount
        val amount = parseAmount(fullText) ?: return null
        
        // Parse account number
        val accountNumber = parseAccountNumber(fullText)
        
        // Detect transaction type (credit/debit)
        val transactionType = detectTransactionType(fullText)
        
        // Parse sender name
        val senderName = parseSenderName(fullText)
        
        return TransactionInfo(
            bankName = bankName,
            transactionType = transactionType,
            amount = amount,
            accountNumber = accountNumber,
            senderName = senderName,
            timestamp = null,
            isQRIS = isQRIS,
            rawText = fullText
        )
    }
    
    private fun isTransactionNotification(text: String): Boolean {
        val keywords = listOf(
            "transfer", "terima", "penerimaan", "mutasi", "saldo", "debit", "kredit",
            "bayar", "pembayaran", "kirim", "tarik", "setor", "QRIS", "masuk", "keluar"
        )
        return keywords.any { text.contains(it, ignoreCase = true) }
    }
    
    private fun parseAmount(text: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(text)
        return if (matcher.find()) {
            try {
                val amountStr = matcher.group(1)?.replace(".", "")?.replace(",", ".") ?: return null
                amountStr.toDoubleOrNull()
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    private fun parseAccountNumber(text: String): String? {
        val matcher = ACCOUNT_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.replace("-", "")
        } else null
    }
    
    private fun detectTransactionType(text: String): String {
        return when {
            text.contains("terima", ignoreCase = true) -> "credit"
            text.contains("masuk", ignoreCase = true) -> "credit"
            text.contains("penerimaan", ignoreCase = true) -> "credit"
            text.contains("kirim", ignoreCase = true) -> "debit"
            text.contains("bayar", ignoreCase = true) -> "debit"
            text.contains("pembayaran", ignoreCase = true) -> "debit"
            text.contains("tarik", ignoreCase = true) -> "debit"
            text.contains("keluar", ignoreCase = true) -> "debit"
            else -> "unknown"
        }
    }
    
    private fun parseSenderName(text: String): String? {
        val namePattern = Pattern.compile(
            "(?:dari|atas nama|a\\.n|nama)\\s*[:.]?\\s*([A-Z][A-Za-z\\s]{2,30})",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = namePattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.trim()
        } else null
    }
}
