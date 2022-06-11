package com.example.fooddine.QrCodeScanner

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.oned.MultiFormatOneDReader
import java.nio.ByteBuffer

class QRCodeAnalyZer( private val onQrCodeScanner: (String) -> Unit): ImageAnalysis.Analyzer {

    private val supportedImageFormats= listOf(
        ImageFormat.YUV_444_888,
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888
    )

    override fun analyze(image: ImageProxy) {
        if(image.format in supportedImageFormats)
        {
            val bytesArray = image.planes.first().buffer.toByteArray()

            val sourceUVL = PlanarYUVLuminanceSource(
                bytesArray,image.width,image.height,0,0,image.width,image.height,false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(sourceUVL))

            try {
                val result= MultiFormatReader().apply {

                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                BarcodeFormat.QR_CODE
                            )
                        )
                    )
                }.decode(binaryBitmap)

                onQrCodeScanner(result.toString())
            }
            catch (e:Exception){
                e.printStackTrace()
            }
            finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray{
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }

}