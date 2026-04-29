package com.example.budget.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.budget.databinding.FragmentScannerBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class ScannerFragment : Fragment() {
    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnCapture.setOnClickListener { 
            binding.btnCapture.isEnabled = false
            takePhoto() 
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("ScannerFragment", "Camera start failed", exc)
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al iniciar cámara", Toast.LENGTH_SHORT).show()
                    binding.btnCapture.isEnabled = true
                }
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    processImage(imageProxy)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("ScannerFragment", "Photo capture failed", exception)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error al capturar foto", Toast.LENGTH_SHORT).show()
                        binding.btnCapture.isEnabled = true
                    }
                }
            }
        )
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (isAdded) extractData(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.e("ScannerFragment", "Text recognition failed", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error al procesar ticket", Toast.LENGTH_SHORT).show()
                        binding.btnCapture.isEnabled = true
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
            if (isAdded) binding.btnCapture.isEnabled = true
        }
    }

    private fun extractData(text: String) {
        val lines = text.split("\n")
        var merchant = ""
        
        // Find Merchant: usually the first non-trivial line
        for (line in lines) {
            val cleaned = line.trim()
            if (cleaned.length > 3 && !cleaned.any { it.isDigit() } && merchant.isEmpty()) {
                merchant = cleaned
                break
            }
        }

        val allAmounts = mutableListOf<Double>()
        val amountPattern = Pattern.compile("(\\d+[.,]\\d{2})")
        val matcher = amountPattern.matcher(text)
        while (matcher.find()) {
            matcher.group(1)?.let {
                val normalized = it.replace(",", ".")
                try { allAmounts.add(normalized.toDouble()) } catch (e: Exception) {}
            }
        }

        var finalAmount = 0.0
        val lowercaseText = text.lowercase()
        
        // Priority logic for TOTAL
        val totalKeywords = listOf("total", "total mxn", "monto total", "pago total", "total a pagar", "neto")
        val changeKeywords = listOf("cambio", "su cambio", "vuelto")
        val paymentKeywords = listOf("efectivo", "recibido", "pago con", "visa", "mastercard")

        var bestTotal = -1.0
        
        // Strategy: Look for lines containing "TOTAL" and a number
        for (line in lines) {
            val lowerLine = line.lowercase()
            if (totalKeywords.any { lowerLine.contains(it) }) {
                val lineMatcher = amountPattern.matcher(line)
                if (lineMatcher.find()) {
                    bestTotal = lineMatcher.group(1)?.replace(",", ".")?.toDouble() ?: -1.0
                    break
                }
            }
        }

        if (bestTotal > 0) {
            finalAmount = bestTotal
        } else if (allAmounts.isNotEmpty()) {
            val sorted = allAmounts.sortedDescending()
            // If "Payment" or "Change" words are present, the total is likely the 2nd largest
            // (Standard sequence: Payment > Total > Change)
            val hasPaymentInfo = paymentKeywords.any { lowercaseText.contains(it) } || changeKeywords.any { lowercaseText.contains(it) }
            
            finalAmount = if (hasPaymentInfo && sorted.size >= 2) {
                // If the largest is huge compared to second (e.g. 500 paid for 45 total), pick second
                if (sorted[0] > sorted[1]) sorted[1] else sorted[0]
            } else {
                sorted[0]
            }
        }

        val resultBundle = Bundle().apply {
            putString("merchant", merchant)
            putDouble("amount", finalAmount)
        }
        
        activity?.runOnUiThread {
            if (isAdded) {
                setFragmentResult("scanner_result", resultBundle)
                findNavController().popBackStack()
            }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {}
        cameraExecutor.shutdown()
        _binding = null
    }
}
