package com.example.appgrupo3.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File



object ImagenUtils {
    private const val CARPETA = "evidencias"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }


    fun crearArchivoFoto(context: Context): Pair<File, Uri> {
        val archivo = File(carpeta(context), "foto_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        return archivo to uri
    }


    fun copiarDesdeGaleria(context: Context, origen: Uri): File? = try {
        val destino = File(carpeta(context), "galeria_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        if (destino.exists() && destino.length() > 0) destino
        else {
            destino.delete()
            null
        }
    } catch (e: Exception) {
        null
    }


    fun borrar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }
}
