package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.DashboardResponse
import com.tesis.gamificacion.service.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/{usuarioId}")
    fun obtenerDashboard(@PathVariable usuarioId: Long): ResponseEntity<DashboardResponse> {
        val dashboard = dashboardService.obtenerDashboard(usuarioId)
        return ResponseEntity.ok(dashboard)
    }
}