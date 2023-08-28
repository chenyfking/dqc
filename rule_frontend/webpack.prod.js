const path = require('path');
const { merge } = require('webpack-merge');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const common = require('./webpack.common.js');

const Visualizer = require('webpack-visualizer-plugin2');
const { BundleStatsWebpackPlugin } = require('bundle-stats-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = merge(common, {
  mode: 'production',
  plugins: [
    new CleanWebpackPlugin(),
    new MiniCssExtractPlugin({ // 抽取js中的css，单独生成文件
      filename: 'static/css/[name].[contenthash].css',
      chunkFilename: 'static/css/[id].[contenthash].css'
    }),
    new CopyWebpackPlugin({ // 拷贝static中不需要编译的资源到dist/static下
      patterns: [
        {
          from: path.resolve(__dirname, 'static'),
          to: 'static'
        }
      ]
    }),
    // Visualizer、BundleStatsWebpackPlugin、BundleAnalyzerPlugin分析bundle占用
    new Visualizer({
      filename: '../bundle-stats/stats.html'
    }),
    new BundleStatsWebpackPlugin({
      baseline: true,
      outDir: '../bundle-stats'
    }),
    new BundleAnalyzerPlugin({
      analyzerMode: 'static',
      reportFilename: '../bundle-stats/report.html',
      openAnalyzer: false
    })
  ],
  module: {
    rules: [
      {
        test: /\.css$/i,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
            options: {
              publicPath: '../../' // 声明background:url(image)方式引用图片路径
            }
          },
          'css-loader'
        ]
      }
    ]
  },
  optimization: {
    moduleIds: 'deterministic',
    runtimeChunk: 'single',
    splitChunks: {
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]((?!monaco-editor).)+/, // 把node_modules中除了monaco-editor其它所有依赖都打到vendors里
          name: 'vendors',
          chunks: 'all'
        },
        monaco: {
          test: /[\\/]node_modules[\\/]monaco-editor[\\/]/, // 把monaco-editor依赖单独打包，import()动态导入
          name: 'monaco-editor',
          chunks: 'all'
        }
      }
    },
    minimizer: [
      `...`,
      new CssMinimizerPlugin()
    ]
  }
});
