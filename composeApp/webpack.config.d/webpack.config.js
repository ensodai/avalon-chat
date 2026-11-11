;(function(config) {
  // Proxy target for local WebAssembly development
  const backendTarget = process.env.APP_SERVER_BASE_URL || 'http://localhost:8080';

  config.devServer = config.devServer || {}
  config.devServer.headers = {
    'Cross-Origin-Opener-Policy': 'same-origin',
    'Cross-Origin-Embedder-Policy': 'require-corp'
  }
  config.devServer.proxy = [
    {
      context: ['/api', '/sync', '/sync-v2', '/uploads'],
      target: backendTarget,
      changeOrigin: true,
      ws: true,
      secure: false,
      headers: {
        'Origin': backendTarget,
        'Version-Code': '82',
        'X-Platform': 'Web'
      },
      onProxyReqWs: function(proxyReq, req, socket, options, head) {
        proxyReq.setHeader('Origin', backendTarget);
        proxyReq.setHeader('Version-Code', '82');
        proxyReq.setHeader('X-Platform', 'Web');
      }
    }
  ]
})(config);
