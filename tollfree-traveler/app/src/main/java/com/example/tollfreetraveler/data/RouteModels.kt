package com.example.tollfreetraveler.data

import org.osmdroid.util.GeoPoint

/**
 * Represents a road segment with toll information
 */
data class RoadSegment(
    val id: String,
    val name: String,
    val roadType: RoadType,
    val distance: Double, // in kilometers
    val estimatedTime: Long, // in seconds
    val hasToll: Boolean = false,
    val tollCost: Double = 0.0, // in local currency
    val surfaceQuality: SurfaceQuality = SurfaceQuality.PAVED,
    val isHighway: Boolean = false,
    val isSecondaryRoad: Boolean = false,
    val startPoint: GeoPoint,
    val endPoint: GeoPoint
)

enum class RoadType {
    HIGHWAY,
    PRIMARY,
    SECONDARY,
    TERTIARY,
    RESIDENTIAL,
    UNCLASSIFIED,
    TRACK,
    SERVICE
}

enum class SurfaceQuality {
    EXCELLENT,      // Smooth highway/new pavement
    GOOD,           // Well-maintained roads
    PAVED,          // Standard paved roads
    FAIR,           // Some wear but passable
    POOR,           // Rough but drivable
    UNPAVED,        // Gravel/dirt
    MUD            // Avoid if possible
}

/**
 * Route calculation options for toll-free navigation
 */
data class RoutingOptions(
    val avoidTolls: Boolean = true,
    val avoidHighways: Boolean = false,
    val preferSecondaryRoads: Boolean = true,
    val minimumSurfaceQuality: SurfaceQuality = SurfaceQuality.PAVED,
    val maxDetourPercentage: Double = 50.0, // Willing to go 50% longer to avoid tolls
    val scenicRoutePreference: Boolean = true
)

/**
 * Complete route with multiple segments
 */
data class Route(
    val segments: List<RoadSegment>,
    val totalDistanceMeters: Double,
    val estimatedTimeMinutes: Long,
    val totalTollCost: Double = 0.0,
    val hasTolls: Boolean = false
) {
    /**
     * Check if this route is completely toll-free
     */
    fun isTollFree(): Boolean = !hasTolls
    
    /**
     * Get the percentage of secondary roads in this route
     */
    fun getSecondaryRoadPercentage(): Double {
        if (segments.isEmpty()) return 0.0
        val secondarySegments = segments.count { 
            it.isSecondaryRoad || it.roadType == RoadType.SECONDARY || it.roadType == RoadType.TERTIARY
        }
        return (secondarySegments.toDouble() / segments.size) * 100
    }
    
    /**
     * Calculate average surface quality
     */
    fun getAverageSurfaceQuality(): SurfaceQuality {
        if (segments.isEmpty()) return SurfaceQuality.PAVED
        
        val qualityScores = segments.map { 
            when(it.surfaceQuality) {
                SurfaceQuality.EXCELLENT -> 6
                SurfaceQuality.GOOD -> 5
                SurfaceQuality.PAVED -> 4
                SurfaceQuality.FAIR -> 3
                SurfaceQuality.POOR -> 2
                SurfaceQuality.UNPAVED -> 1
                SurfaceQuality.MUD -> 0
            }
        }
        
        val avgScore = qualityScores.average()
        
        return when {
            avgScore >= 5.5 -> SurfaceQuality.EXCELLENT
            avgScore >= 4.5 -> SurfaceQuality.GOOD
            avgScore >= 3.5 -> SurfaceQuality.PAVED
            avgScore >= 2.5 -> SurfaceQuality.FAIR
            avgScore >= 1.5 -> SurfaceQuality.POOR
            avgScore >= 0.5 -> SurfaceQuality.UNPAVED
            else -> SurfaceQuality.MUD
        }
    }
}
