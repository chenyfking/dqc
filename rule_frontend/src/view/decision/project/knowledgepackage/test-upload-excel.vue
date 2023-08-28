<template>
  <div>
    <el-upload
      action
      :on-change="uploadExcel"
      :auto-upload="false"
      :show-file-list="false"
      accept=".xls, .xlsx, .csv"
    >
      <el-button icon="el-icon-upload2">上传批量测试数据</el-button>
    </el-upload>

    <el-dialog
      title="批量测试知识包"
      width="70%"
      :visible.sync="showBatchDialog"
      append-to-body
      :close-on-click-modal="false"
    >
      <div class="toolbar">
        <el-button
          type="primary"
          icon="el-icon-caret-right"
          @click="submitBatchTest"
          :loading="batchLoading"
        >{{batchLoading ? '提交中...' : '批量测试'}}</el-button>
        <el-upload
          action
          style="display: inline-block;"
          :on-change="uploadExcel"
          :auto-upload="false"
          :show-file-list="false"
          accept=".xlsx"
        >
          <el-button icon="el-icon-upload" class="importService">重新上传</el-button>
        </el-upload>
        <!--<el-button-->
          <!--type="primary"-->
          <!--v-if="isDown"-->
          <!--:loading="opLoading"-->
          <!--icon="el-icon-download"-->
          <!--@click="download"-->
        <!--&gt;预测结果下载</el-button>-->
      </div>

      <el-alert
        v-show="batchData.result"
        class="toolbar"
        title="测试成功"
        :closable="false"
        type="success"
        show-icon
      >
        <p class="el-alert__description" v-html="batchData.resultHtml"></p>
      </el-alert>

      <el-row v-loading="testDataLoading">
        <el-col :span="24">
          <el-tabs type="border-card">
            <el-tab-pane :label="data.name" v-for="(data, index) in batchTest" :key="index">
              <el-table :data="data.data" border v-if="data.data[0] != undefined">
                <el-table-column type="index" width="50"></el-table-column>
                <el-table-column
                  v-for="(columnItem, key, i) in data.data[0]"
                  :key="key"
                  :label="batchTest[index].batchTableHead[i]"
                  align="center"
                >
                  <template slot-scope="scope">{{scope.row[batchTest[index].batchTableHead[i]]}}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "test-upload-excel",
  computed: {
    pkg() {
      return this.$store.state.pkg;
    }
  },
  data() {
    return {
      showBatchDialog: false,
      batchLoading: false,
      batchData: {
        activeIndex: 0,
        facts: null
      },
      batchTest: [],
      testDataLoading: false,
      isDown: false,
      opLoading: false
    };
  },
  methods: {
    uploadExcel(e) {
      let form = new FormData();
      form.append("file", e.raw);
      form.append("uuid", this.pkg.uuid);
      this.batchData = {
        activeIndex: 0,
        facts: null
      };
      this.$axios
        .post("/knowledgepackage/importbatchdata", form)
        .then(res => {
          if (res.data.code == 0) {
            let data = res.data.data;
            for (let i in data) {
              let batchTableHead = [];
              if (data[i].data[0] == undefined) {
                for (let j in data[i].data) {
                  batchTableHead.push(j);
                }
              } else {
                for (let j in data[i].data[0]) {
                  batchTableHead.push(j);
                }
              }
              data[i].batchTableHead = batchTableHead;
            }
            this.batchTest = data;
            this.showBatchDialog = true;
          } else {
            this.$message.error( res.data.msg ? res.data.msg : "测试失败")
          }
        })
        .catch(() => {
           this.$message.error("操作失败")
        });
    },
    download(row) {
      this.opLoading = true;
      const that = this;
      const params = that.batchData.facts;
      this.$axios({
        method: "post",
        url: "/knowledgepackage/exportresult",
        responseType: "blob",
        data: params
      })
        .then(res => {
          let url = window.URL.createObjectURL(new Blob([res.data]));
          let a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          a.target = "_blank";
          a.setAttribute("download", '预测结果.xls');
          document.body.appendChild(a);
          a.click();
        })
        .catch(() => {
          that.$message.error("下载失败");
        })
        .then(() => {
          this.opLoading = false;
        });
    },
    submitBatchTest() {
      this.batchLoading = true;
      this.$axios
        .post(
          "/knowledgepackage/batchtest",
          this.$qs.stringify({
            uuid: this.pkg.uuid,
            baselineVersion: this.pkg.baselineVersion,
            facts: JSON.stringify([...this.batchTest])
          })
        )
        .then(res => {
          if (res.data.code == 0) {
            const data = res.data.data;
            if (typeof data.result.fireNum === "undefined") {
              data.result.fireNum = 0;
            }
            this.batchData.result = data.result;
            this.batchData.facts = data.facts;
            this.isDown = true;
            let resultHtml = `<p>触发了<b>${data.result.fireNum}</b>条规则，耗时<b>${data.result.fireTime}毫秒</b></p>`;
            if (data.result.fireNum > 0) {
              resultHtml += `<p>规则列表：</p>`;
              data.result.fireRules.forEach((e, i) => {
                resultHtml += `<p style="margin-left: 20px;">${e}</p>`;
              });
            }
            let batchTest = [];
            for (let i in data.facts) {
              for (let j in this.batchTest) {
                batchTest[j] = {
                  name: this.batchTest[j].name,
                  data: data.facts[this.batchTest[j].name]
                };
              }
            }
            let batchTestInfo = [];
            for (let i in batchTest) {
              batchTestInfo[i] = {};
              batchTestInfo[i].batchTableHead = [];
              batchTestInfo[i].data = [];
              for (let j in batchTest[i].data) {
                let data = {};
                if (j == 0) {
                  for (let k in batchTest[i].data[j].fields) {
                    batchTestInfo[i].batchTableHead.push(
                      batchTest[i].data[j].fields[k].label
                    );
                  }
                }
                for (let k in batchTest[i].data[j].fields) {
                  data[batchTest[i].data[j].fields[k].label] =
                    batchTest[i].data[j].fields[k].value;
                }
                batchTestInfo[i].data.push(data);
              }
              batchTestInfo[i].name = batchTest[i].name;
            }
            this.batchTest = batchTestInfo;
            this.batchData.resultHtml = resultHtml;
          } else {
           this.$message.error( res.data.msg ? res.data.msg : "测试失败")
          }
        })
        .catch(() => {
         this.$message.error(  "测试失败")
        })
        .then(() => {
          this.batchLoading = false;
        });
    }
  }
};
</script>

<style scoped>
</style>
