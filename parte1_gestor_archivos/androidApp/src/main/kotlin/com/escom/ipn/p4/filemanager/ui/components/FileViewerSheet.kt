package com.escom.ipn.p4.filemanager.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.escom.ipn.p4.filemanager.model.FileCategory
import com.escom.ipn.p4.filemanager.model.FileItem
import com.escom.ipn.p4.filemanager.service.FileSyntaxHighlighter
import com.escom.ipn.p4.filemanager.service.SyntaxTokenType
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxBooleanColor
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxCommentColor
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxKeyColor
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxNumberColor
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxStringColor
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxXmlAttrNameColor
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxXmlAttrValColor
import com.escom.ipn.p4.filemanager.ui.theme.SyntaxXmlTagColor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerSheet(
    item: FileItem,
    content: String?,
    tiltPanOffset: Offset = Offset.Zero,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TopAppBar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${item.formattedSize}  •  ${item.mimeType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar visor")
                        }
                    },
                    actions = {
                        if (content != null && (item.category == FileCategory.TEXT || item.category == FileCategory.JSON || item.category == FileCategory.XML)) {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(content))
                                Toast.makeText(context, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar contenido")
                            }
                        }
                        IconButton(onClick = { shareFile(context, item) }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir archivo")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )

                // Contenido según categoría
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (item.category) {
                        FileCategory.JSON -> {
                            SyntaxHighlightedCodeView(
                                rawText = content ?: "",
                                isJson = true
                            )
                        }
                        FileCategory.XML -> {
                            SyntaxHighlightedCodeView(
                                rawText = content ?: "",
                                isJson = false
                            )
                        }
                        FileCategory.TEXT -> {
                            PlainTextViewer(text = content ?: "(Archivo vacío)")
                        }
                        FileCategory.IMAGE -> {
                            ImageViewerWithZoom(imagePath = item.path, tiltPanOffset = tiltPanOffset)
                        }
                        else -> {
                            UnsupportedFormatView(item = item, onShare = { shareFile(context, item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyntaxHighlightedCodeView(rawText: String, isJson: Boolean) {
    val scrollState = rememberScrollState()

    val annotated = remember(rawText, isJson) {
        val tokens = if (isJson) {
            FileSyntaxHighlighter.highlightJson(rawText)
        } else {
            FileSyntaxHighlighter.highlightXml(rawText)
        }

        buildAnnotatedString {
            tokens.forEach { token ->
                val style = when (token.type) {
                    SyntaxTokenType.KEY -> SpanStyle(color = SyntaxKeyColor, fontWeight = FontWeight.Bold)
                    SyntaxTokenType.STRING -> SpanStyle(color = SyntaxStringColor)
                    SyntaxTokenType.NUMBER -> SpanStyle(color = SyntaxNumberColor, fontWeight = FontWeight.Medium)
                    SyntaxTokenType.BOOLEAN -> SpanStyle(color = SyntaxBooleanColor, fontWeight = FontWeight.Bold)
                    SyntaxTokenType.NULL -> SpanStyle(color = Color.Gray, fontWeight = FontWeight.Bold)
                    SyntaxTokenType.XML_TAG -> SpanStyle(color = SyntaxXmlTagColor, fontWeight = FontWeight.Bold)
                    SyntaxTokenType.XML_ATTR_NAME -> SpanStyle(color = SyntaxXmlAttrNameColor, fontWeight = FontWeight.SemiBold)
                    SyntaxTokenType.XML_ATTR_VALUE -> SpanStyle(color = SyntaxXmlAttrValColor)
                    SyntaxTokenType.COMMENT -> SpanStyle(color = SyntaxCommentColor, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    SyntaxTokenType.PUNCTUATION -> SpanStyle(color = Color(0xFF424242), fontWeight = FontWeight.Bold)
                    else -> SpanStyle(color = Color(0xFF212121))
                }
                withStyle(style) {
                    append(token.text)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = annotated,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun PlainTextViewer(text: String) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ImageViewerWithZoom(
    imagePath: String,
    tiltPanOffset: Offset = Offset.Zero
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offset += panChange
    }

    val bitmap = remember(imagePath) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de controles de Zoom y Rotación
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Zoom: ${kotlin.math.round(scale * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row {
                    FilledTonalButton(
                        onClick = { rotation = (rotation + 90f) % 360f },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rotar 90°", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                            rotation = 0f
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reiniciar", fontSize = 12.sp)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .transformable(state = transformState),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                val effectiveOffsetX = offset.x + if (scale > 1.2f) tiltPanOffset.x else 0f
                val effectiveOffsetY = offset.y + if (scale > 1.2f) tiltPanOffset.y else 0f

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Visualizador de imagen interactivo",
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = effectiveOffsetX,
                            translationY = effectiveOffsetY,
                            rotationZ = rotation
                        )
                )
            } else {
                Text(
                    text = "No se pudo cargar la imagen desde la ruta especificada.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun UnsupportedFormatView(
    item: FileItem,
    onShare: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Formato no visualizable directamente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Este archivo tiene una extensión o formato (${item.extension}) que no cuenta con un visor de texto integrado.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Metadatos disponibles:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Nombre: ${item.name}", style = MaterialTheme.typography.bodySmall)
                Text("• Tamaño: ${item.formattedSize} (${item.sizeBytes} bytes)", style = MaterialTheme.typography.bodySmall)
                Text("• Tipo MIME: ${item.mimeType}", style = MaterialTheme.typography.bodySmall)
                Text("• Modificado: ${item.formattedDate}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { openWithSystemApp(context, item) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Abrir en otra app")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartir")
            }
        }
    }
}

private fun shareFile(context: Context, item: FileItem) {
    try {
        val file = File(item.path)
        if (!file.exists()) {
            Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = item.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir ${item.name}"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun openWithSystemApp(context: Context, item: FileItem) {
    try {
        val file = File(item.path)
        if (!file.exists()) {
            Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, item.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(viewIntent, "Abrir con"))
    } catch (e: Exception) {
        Toast.makeText(context, "No hay aplicación disponible para abrir este tipo de archivo.", Toast.LENGTH_LONG).show()
    }
}
