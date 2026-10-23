package com.example.tollfreetraveler

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.example.tollfreetraveler.data.RoutingOptions
import com.example.tollfreetraveler.data.SurfaceQuality
import com.example.tollfreetraveler.routing.TollFreeRouteCalculator

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private var isTollFreeMode = true
    private var startPoint: GeoPoint? = null
    private var endPoint: GeoPoint? = null
    
    private val routeCalculator = TollFreeRouteCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Required for OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        
        setContentView(R.layout.activity_main)

        // Setup the OpenStreetMap view
        map = findViewById(R.id.map)
        setupMap()
        
        // Setup UI controls
        setupUI()
        
        // Request location permission
        requestLocationPermission()
    }
    
    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        
        // Default to NYC but will use actual location when permission granted
        val defaultLocation = GeoPoint(40.7128, -74.0060)
        map.controller.setZoom(12.0)
        map.controller.setCenter(defaultLocation)
        
        // Add map click listener for setting destinations
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { point ->
                    if (startPoint == null) {
                        // First tap sets start point
                        startPoint = point
                        addMarker(point, "Start")
                        Snackbar.make(findViewById(android.R.id.content), 
                            "Start point set. Tap again for destination.", 
                            Snackbar.LENGTH_LONG).show()
                    } else if (endPoint == null) {
                        // Second tap sets end point and calculates route
                        endPoint = point
                        addMarker(point, "Destination")
                        calculateRoute()
                    } else {
                        // Reset and start over
                        resetRoute()
                        startPoint = point
                        addMarker(point, "Start")
                        Snackbar.make(findViewById(android.R.id.content), 
                            "New route started. Tap for destination.", 
                            Snackbar.LENGTH_LONG).show()
                    }
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(mapEventsOverlay)
    }
    
    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(map)
        marker.position = point
        marker.title = title
        map.overlays.add(marker)
        map.invalidate()
    }
    
    private fun resetRoute() {
        map.overlays.clear()
        startPoint = null
        endPoint = null
    }
    
    private fun setupUI() {
        // Toggle button for toll-free mode
        findViewById<FloatingActionButton>(R.id.btnToggleMode).setOnClickListener {
            isTollFreeMode = !isTollFreeMode
            updateModeButton()
            Toast.makeText(
                this,
                if (isTollFreeMode) "Toll-Free Mode ON - Avoiding paid roads!" else "Fastest Route Mode",
                Toast.LENGTH_SHORT
            ).show()
            
            // Recalculate route if both points are set
            if (startPoint != null && endPoint != null) {
                calculateRoute()
            }
        }
        
        updateModeButton()
    }

    private fun updateModeButton() {
        val btn = findViewById<FloatingActionButton>(R.id.btnToggleMode)
        btn.contentDescription = if (isTollFreeMode) {
            "Toll-Free Mode Active"
        } else {
            "Fastest Route Mode Active"
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                enableMyLocation()
            }
            else -> {
                Toast.makeText(this, "Location permission denied - using default location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // In a real app, you'd get the actual location here
            // For now, we'll just note that permission was granted
            Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun calculateRoute() {
        startPoint?.let { start ->
            endPoint?.let { end ->
                lifecycleScope.launch {
                    try {
                        val options = RoutingOptions(
                            avoidTolls = isTollFreeMode,
                            preferSecondaryRoads = isTollFreeMode,
                            minimumSurfaceQuality = if (isTollFreeMode) SurfaceQuality.PAVED else SurfaceQuality.ANY
                        )
                        
                        val route = routeCalculator.calculateRoute(start, end, options)
                        
                        if (route.segments.isNotEmpty()) {
                            // Draw the route on map
                            val polyline = Polyline()
                            val routePoints = route.segments.flatMap { segment ->
                                listOf(segment.startPoint, segment.endPoint)
                            }.distinct()
                            polyline.setPoints(routePoints)
                            
                            // Style the polyline
                            polyline.color = if (isTollFreeMode) 0xFF4CAF50.toInt() else 0xFF2196F3.toInt()
                            polyline.width = 8f
                            
                            map.overlays.add(polyline)
                            map.invalidate()
                            
                            // Show route info
                            val distanceKm = route.totalDistanceMeters / 1000.0
                            val timeMin = route.estimatedTimeMinutes
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Route: ${String.format("%.1f", distanceKm)} km, ~${timeMin} min\n" +
                                if (isTollFreeMode) "✓ No tolls, scenic route!" else "Fastest route",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(this@MainActivity, "No route found", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Error calculating route: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        map.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
