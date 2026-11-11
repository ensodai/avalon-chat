;(function(config) {
    const path = require('path');
    const CopyWebpackPlugin = require('copy-webpack-plugin');

    const nodeModulesPath = path.resolve(__dirname, '../../node_modules');

    config.plugins.push(
        new CopyWebpackPlugin({
            patterns: [
                { from: path.join(nodeModulesPath, '@sqlite.org/sqlite-wasm/dist/sqlite3-opfs-async-proxy.js'), to: '.' }
            ]
        })
    );

    config.resolve = config.resolve || {};
    config.resolve.fallback = config.resolve.fallback || {};
    config.resolve.fallback.crypto = false;
    config.resolve.fallback.fs = false;
    config.resolve.fallback.path = false;
})(config);
