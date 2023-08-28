<template>
  <el-dialog
    title="查看报表"
    :top="$dialogTop"
    :visible="show"
    @update:visible="close"
    @open="search"
    width="70%">
    <el-form inline>
      <el-form-item label="开始时间">
        <el-date-picker
          style="width:180px"
          v-model="startDate"
          type="date"
          :picker-options="startDatePickerOptions"
          placeholder="开始日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="endDate"
          style="width:180px"
          type="date"
          placeholder="结束时间"
          :picker-options="endDatePickerOptions">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="search">查 询</el-button>
      </el-form-item>
      <el-form-item v-if="$hasPerm('report:export')">
        <el-button icon="el-icon-download" @click="exportReport">导 出</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="reports">
      <el-table-column label="模型">
        <template slot-scope="scope">
          {{scope.row.pkgBaseline ? 'V' + scope.row.pkgBaseline : '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="reqCount" label="调用次数"></el-table-column>
      <el-table-column prop="batchReqCount" label="批量调用次数"></el-table-column>
      <el-table-column prop="batchReqSuccessRatio" label="批量调用正确率">
        <template slot-scope="scope">
          {{scope.row.batchReqSuccessRatio ? scope.row.batchReqSuccessRatio + "%" : "-"}}
        </template>
      </el-table-column>
      <el-table-column prop="onlineReqCount" label="联机调用次数"></el-table-column>
      <el-table-column prop="onlineReqSuccessRatio" label="联机调用正确率">
        <template slot-scope="scope">
          {{scope.row.onlineReqSuccessRatio ? scope.row.onlineReqSuccessRatio + "%" : "-"}}
        </template>
      </el-table-column>
      <el-table-column prop="reqPassSuccessRatio" label="调用通过率">
        <template slot-scope="scope">
          {{scope.row.reqPassSuccessRatio ? scope.row.reqPassSuccessRatio + "%" : "-"}}
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      background
      layout="total, prev, pager, next, jumper"
      :total="total"
      hide-on-single-page
      :current-page.sync="tablePage"
      @current-change="search">
    </el-pagination>
  </el-dialog>
</template>

<script>
  export default {
    name: "report",
    props: {
      deploymentUuid: String,
      show: Boolean
    },
    data() {
      return {
        tableLoading: false,
        reports: [],
        tablePage: 1,
        total: 0,
        startDate: '',
        endDate: '',
        startDatePickerOptions: {
          disabledDate: (time) => {
            if (this.endDate != "") {
              return (
                time.getTime() > Date.now() ||
                time.getTime() > this.endDate
              );
            } else {
              return time.getTime() > Date.now();
            }
          },
        },
        endDatePickerOptions: {
          disabledDate: (time) => {
            return (
              time.getTime() < this.startDate ||
              time.getTime() > Date.now()
            );
          },
        },
      };
    },
    methods: {
      search() {
        this.tableLoading = true;
        let params = {
          keywords: this.deploymentUuid,
          page: this.tablePage
        }
        if (this.startDate) {
          params.startDate = this.$moment(this.startDate).format("YYYY-MM-DD");
        }
        if (this.endDate) {
          params.endDate = this.$moment(this.endDate).format("YYYY-MM-DD");
        }
        this.$axios.get("/report/deployment", {params}).then(res => {
          this.tableLoading = false;
          if (res.data.code == 0) {
            this.reports = res.data.data;
            this.total = res.data.total;
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "系统异常");
          }
        }).catch(() => {
          this.$message.error("系统异常");
          this.tableLoading = false;
        });
      },
      exportReport() {
        const that = this;
        let params = {
          startDate: this.startDate,
          endDate: this.endDate
        }
        if (params.startDate) {
          params.startDate = this.$moment(this.startDate).format("YYYY-MM-DD");
        }
        if (params.endDate) {
          params.endDate = this.$moment(this.endDate).format("YYYY-MM-DD");
        }
        const url = `/report/exportDeployment?keywords=${this.deploymentUuid}&startDate=${params.startDate}&endDate=${params.endDate}`
        that.tableLoading = true
        this.$axios({
          method: "get",
          url: url,
          responseType: "blob",
        }).then((res) => {
            if (res.data.type == "application/json") {
              const reader = new FileReader();
              reader.onload = function () {
                const json = JSON.parse(reader.result);
                if (json.code == 401) {
                  localStorage.removeItem('hasLogin')
                  const from = that.$router.currentRoute.fullPath
                  that.$router.push('/login?from=' + from)
                } else {
                  that.$message.error(json.msg ? json.msg : "导出失败");
                }
              };
              reader.readAsText(res.data);
            } else {
              let url = window.URL.createObjectURL(new Blob([res.data]));
              let a = document.createElement("a");
              a.style.display = "none";
              a.href = url;
              a.target = "_blank";
              a.setAttribute("download", '统计报表.xlsx');
              document.body.appendChild(a);
              a.click();
            }
            that.tableLoading = false;
          })
          .catch(() => {
            that.$message.error("导出失败");
            that.tableLoading = false;
          });
      },
      close() {
        this.$emit("update:show", false);
      }
    }
  }
</script>

<style scoped>
</style>
