namespace BembosEvents.Web.Models.MercadoPago;

public sealed class MercadoPagoPaymentDto
{
    public int IdPago { get; init; }

    public int IdReserva { get; init; }

    public decimal Monto { get; init; }

    public string MetodoPago { get; init; } =
        string.Empty;

    public string EstadoPago { get; init; } =
        string.Empty;

    public string? ReferenciaExterna { get; init; }

    public DateTime? FechaPago { get; init; }

    public DateTime FechaCreacion { get; init; }
}

public sealed class MercadoPagoCheckoutRequestDto
{
    public decimal Monto { get; init; }
}

public sealed class MercadoPagoCheckoutDto
{
    public MercadoPagoPaymentDto? Pago { get; init; }

    public string Proveedor { get; init; } =
        string.Empty;

    public string OrderId { get; init; } =
        string.Empty;

    public string OrderStatus { get; init; } =
        string.Empty;

    public string CheckoutUrl { get; init; } =
        string.Empty;
}

public sealed class MercadoPagoRefundDto
{
    public MercadoPagoPaymentDto? Pago { get; init; }

    public string Proveedor { get; init; } =
        string.Empty;

    public string OrderId { get; init; } =
        string.Empty;

    public string ProviderStatus { get; init; } =
        string.Empty;

    public string ProviderStatusDetail { get; init; } =
        string.Empty;

    public bool ProviderCallExecuted { get; init; }
}

public sealed class MercadoPagoReconciliationItemDto
{
    public int IdPago { get; init; }

    public string OrderId { get; init; } =
        string.Empty;

    public string EstadoLocalAnterior { get; init; } =
        string.Empty;

    public string? ProviderStatus { get; init; }

    public string? ProviderStatusDetail { get; init; }

    public string? EstadoObjetivo { get; init; }

    public string Resultado { get; init; } =
        string.Empty;

    public string Mensaje { get; init; } =
        string.Empty;
}

public sealed class MercadoPagoReconciliationDto
{
    public int Total { get; init; }

    public int Actualizados { get; init; }

    public int EnSincronia { get; init; }

    public int SinCambio { get; init; }

    public int Conflictos { get; init; }

    public int Errores { get; init; }

    public List<MercadoPagoReconciliationItemDto> Items { get; init; } =
        new();
}