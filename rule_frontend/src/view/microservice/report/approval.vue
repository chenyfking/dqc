<template>
  <el-card>
    <el-form inline :model="form">
      <el-form-item label="关键词">
        <el-input
          style="width:170px"
          v-model="form.keywords"
          @keyup.enter.native="query"
          placeholder="请输入关键词">
        </el-input>
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker
          style="width:180px"
          v-model="form.startDate"
          type="date"
          placeholder="开始时间"
          :picker-options="startDatePickerOptions">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="form.endDate"
          style="width:180px"
          type="date"
          placeholder="结束时间"
          :picker-options="endDatePickerOptions">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="query">查 询</el-button>
      </el-form-item>
      <el-form-item v-if="$hasPerm('report:export')">
        <el-button icon="el-icon-download" @click="exportReport">导 出</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="list">
      <el-table-column prop="ruleName" label="规则名称"></el-table-column>
      <el-table-column prop="ownerUserName" label="归属用户"></el-table-column>
      <el-table-column prop="ownerOrgName" label="归属机构"></el-table-column>
      <el-table-column prop="reqCount" label="调用次数"></el-table-column>
      <el-table-column prop="reqPassCountRatio" label="调用通过率">
        <template slot-scope="scope">
          {{scope.row.reqPassCountRatio ? scope.row.reqPassCountRatio + "%" : "-"}}
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      background
      layout="total, prev, pager, next, jumper"
      :total="total"
      hide-on-single-page
      :page-size="10"
      @current-change="toPage">
    </el-pagination>
  </el-card>
</template>

<script>
export default {
  name: "index",
  data() {
    return {
      tableLoading: false,
      list: [],
      tablePage: 1,
      total: 0,
      form: {
        keywords: "",
        startDate: "",
        endDate: ""
      },
      startDatePickerOptions: {
        disabledDate: (time) => {
          if (this.form.endDate != "") {
            return (
              time.getTime() > Date.now() ||
              time.getTime() > this.form.endDate
            );
          } else {
            return time.getTime() > Date.now();
          }
        },
      },
      endDatePickerOptions: {
        disabledDate: (time) => {
          return (
            time.getTime() < this.form.startDate ||
            time.getTime() > Date.now()
          );
        },
      },
    };
  },
  mounted() {
    this.query();
  },
  methods: {
    toPage(page) {
      this.tablePage = page;
      this.query();
    },
    query() {
      this.tableLoading = true;
      let params = this.$utils.copy(this.form)
      if (params.startDate) {
        params.startDate = this.$moment(params.startDate).format("YYYY-MM-DD");
      }
      if (params.endDate) {
        params.endDate = this.$moment(params.endDate).format("YYYY-MM-DD");
      }
      params.page = this.tablePage;
      params.pageNum = 10;
      this.$axios.get("/report/approval", { params }).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          this.list = res.data.data;
          this.total = res.data.total;
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "系统异常");
        }
      }).catch(() => {
        this.$message.error(res.data.msg ? res.data.msg : "系统异常");
        this.tableLoading = false;
      });
    },
    exportReport() {
      const that = this;
      let params = this.$utils.copy(this.form)
      if (params.startDate) {
        params.startDate = this.$moment(params.startDate).format("YYYY-MM-DD");
      }
      if (params.endDate) {
        params.endDate = this.$moment(params.endDate).format("YYYY-MM-DD");
      }
      this.$axios({
        method: "get",
        url:`/report/exportApproval?keywords=${params.keywords}&startDate=${params.startDate}&endDate=${params.endDate}` ,
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
          a.setAttribute("download", '审批类报表.xlsx');
          document.body.appendChild(a);
          a.click();
        }
        that.tableLoading = false;
      }).catch(() => {
        that.$message.error("导出失败");
        that.tableLoading = false;
      });
    },
  },
};
</script>

<style >
.timePickIn .el-input__inner {
  width: 220px;
}
</style>
