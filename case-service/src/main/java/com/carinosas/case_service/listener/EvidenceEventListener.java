package com.carinosas.case_service.listener;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EvidenceEventListener {

    // Este @RabbitListener crea la cola automáticamente si no existe y se pone a escuchar
    @RabbitListener(queuesToDeclare = @Queue("evidence.queue"))
    public void listenForNewEvidence(String mensaje) {
        // Aquí podrías buscar el caso en BD y actualizar su campo "updatedAt"
        // Por ahora, lo imprimiremos en la consola como un log forense:
        System.out.println("🚨 [ALERTA DE SISTEMA] Escuchado por el Case Service: " + mensaje);
    }
}