package com.example.gemstoneai.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

data class LabelPrediction(
    val text: String,
    val confidence: Float
)

data class AnalysisResult(
    val predictions: List<LabelPrediction>
)

class GemAnalyzer {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    suspend fun analyze(bitmap: Bitmap): AnalysisResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labels = labeler.process(image).await()

        val preds = labels
            .map { LabelPrediction(text = it.text, confidence = it.confidence) }
            .sortedByDescending { it.confidence }

        return AnalysisResult(predictions = preds)
    }
}
