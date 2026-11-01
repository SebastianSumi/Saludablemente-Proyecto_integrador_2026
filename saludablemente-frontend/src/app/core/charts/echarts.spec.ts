describe('Gráficos · catálogo cargado de forma diferida', () => {
  it.each(['bar', 'pie'])('registra el gráfico %s al cargar el módulo', async (type) => {
    const { echarts } = await import('./echarts');
    // El entorno de prueba no tiene canvas para medir texto.
    echarts.setPlatformAPI({ measureText: text => ({ width: text.length * 7 }) });
    // SVG solo para esta prueba sin canvas de navegador; producción conserva CanvasRenderer.
    const { SVGRenderer } = await import('echarts/renderers');
    echarts.use([SVGRenderer]);
    const chart = echarts.init(null, undefined, { renderer: 'svg', ssr: true, width: 320, height: 200 });
    try {
      chart.setOption({
        ...(type === 'bar' ? { xAxis: { type: 'category', data: ['Comunidad'] }, yAxis: {} } : {}),
        series: [{ type, data: [2, 3] }],
      });
      expect(chart.getOption()['series']).toEqual(expect.arrayContaining([expect.objectContaining({ type })]));
      expect(chart.renderToSVGString()).toContain('<svg');
    } finally {
      chart.dispose();
    }
  });
});
