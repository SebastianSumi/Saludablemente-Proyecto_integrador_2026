package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.response;

public record CardioResponse(
        Integer repeticionesSoga30s,
        Integer tiempoCarrera400mSeg,
        String dxCardiovascular
) {}