<template>
  <div>
    <el-page-header content="查看调用方式" @back="() => $router.go(-1)"></el-page-header>

    <el-card v-loading="loading" element-loading-text="数据加载中...">
      <h4 class="h4-title">接入点 / API point</h4>
      <div class="content">
        <p>接口地址：<span>{{address}}</span></p>
        <p>请求格式：<span>JSON</span></p>
        <p>返回格式：<span>JSON</span></p>
        <p>请求方式：<span>HTTP协议，支持POST方法</span></p>
      </div>
      <br/>

      <h4 class="h4-title">请求示例 / Request example</h4>
      <pre class="language-javascript"><code class="language-javascript">curl -H <span class="token string">"Content-Type:application/json"</span> -H <span class="token string">"token:${token}"</span> -X POST --data <span
        class="token string">'{{requestExample}}'</span> {{address}}</code></pre>
      <br/>
      <h4 class="h4-title">返回示例 / Response example</h4>
      <pre class="language-json"><code class="language-json">{{responseExample}}</code></pre>
    </el-card>
  </div>
</template>

<script>
  export default {
    name: "index",
    data() {
      return {
        address: '',
        service: '',
        requestExample: '',
        responseExample: '',
        loading: false
      }
    },
    mounted() {
      this.loading = true
      this.$axios.get('/micro/apidoc', {
        params: {
          uuid: this.$route.params.uuid
        }
      }).then(res => {
        this.loading = false
        if (res.data.code == 0) {
          const data = res.data.data
          this.address = data.serviceUrl
          this.service = data.serviceId
          this.requestExample = JSON.stringify(data.input.data)
          this.responseExample = data.output.result
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '查询出错')
        }
      }).catch(() => {
        this.loading = false
      })
    }
  }
</script>

<style scoped>
  .h4-title {
    border-left: 2px solid #45ad7a;
    padding-left: 10px;
    font-weight: 600 !important;
    margin: 0px 0px 15px;
    font-family: Open Sans, sans-serif;
    font-size: 18px;
    line-height: 1.1;
    color: inherit;
  }

  .content {
    margin: 10px;
    font-size: 14px;
  }

  .content > p {
    height: 35px;
    line-height: 35px;
    border-bottom: 1px dashed #eaeaea;
    margin: 0;
    padding: 0 10px;
  }

  .content > p > span {
    color: #999;
    margin-left: 10px;
  }

  .table {
    width: 100%;
    max-width: 100%;
    margin-bottom: 20px;
    border-spacing: 0;
    border-collapse: collapse;
  }

  .table-advance thead {
    color: #999;
  }

  .table-advance thead tr th {
    background-color: #eaeaea;
    font-size: 14px;
    font-weight: 400;
    color: #666;
  }

  .table-striped > tbody > tr:nth-of-type(odd) {
    background-color: #fff;
  }

  .main-text {
    color: #999;
    line-height: 1.8;
  }

  :not(pre) > code[class*=language-], pre[class*=language-] {
    background: #f5f2f0;
  }

  pre[class*=language-] {
    padding: 1em;
    margin: .5em 0;
    overflow: auto;
    border: 1px solid #ccc;
    border-radius: 0px;
  }

  code[class*=language-], pre[class*=language-] {
    color: #000;
    background: none;
    text-shadow: 0 1px #fff;
    font-family: Consolas, Monaco, Andale Mono, Ubuntu Mono, monospace;
    text-align: left;
    white-space: pre;
    word-spacing: normal;
    word-break: normal;
    word-wrap: normal;
    line-height: 1.5;
    -moz-tab-size: 4;
    -o-tab-size: 4;
    tab-size: 4;
    -webkit-hyphens: none;
    -ms-hyphens: none;
    hyphens: none;
  }

  .token.attr-name, .token.builtin, .token.char, .token.inserted, .token.selector, .token.string {
    color: #690;
  }

  .table-bordered {
    border: 1px solid #ddd;
  }

  .table>thead:first-child>tr:first-child>th {
    border-top: 0;
  }

  .table-bordered>thead>tr>td, .table-bordered>thead>tr>th {
    border-bottom-width: 2px;
  }

  .table-bordered>thead>tr>th {
    border: 1px solid #ddd;
  }

  .table>thead>tr>th {
    vertical-align: bottom;
    border-bottom: 2px solid #ddd;
  }

  .table>thead>tr>th {
    padding: 8px;
    line-height: 1.42857143;
    vertical-align: top;
    border-top: 1px solid #ddd;
  }

  td, code {
    color: #666 !important;
  }
</style>
