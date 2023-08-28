<template>
  <div>
      <el-form inline :model="form" ref="form">
        <el-form-item label="操作描述" class="timePickIn" prop="optName">
          <el-input v-model="form.optName" placeholder="操作描述" @keyup.enter.native="search"></el-input>
        </el-form-item>
        <el-form-item label="操作地址" class="timePickIn" prop="clientIp">
          <el-input v-model="form.clientIp" placeholder="操作地址" @keyup.enter.native="search"></el-input>
        </el-form-item>
        <el-form-item label="是否成功" prop="success">
          <el-select
            v-model="form.success"
            placeholder="是否成功">
            <el-option label="全部" value></el-option>
            <el-option label="成功" value="1"></el-option>
            <el-option label="失败" value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="beginTime">
          <el-date-picker
            v-model="form.beginTime"
            type="date"
            placeholder="开始日期"
            :picker-options="beginTimePickerOptions">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="date"
            placeholder="结束日期"
            :picker-options="endTimePickerOptions">
          </el-date-picker>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="search" icon="el-icon-search">查 询</el-button>
          <el-button @click="reset" icon="el-icon-refresh">重 置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="logs" align="center">
        <el-table-column prop="optName" label="操作描述"></el-table-column>
        <el-table-column prop="user" label="操作用户"></el-table-column>
        <el-table-column prop="clientIp" label="操作地址"></el-table-column>
        <el-table-column prop="beginTime" label="操作时间"></el-table-column>
        <el-table-column prop="useTime" label="请求用时(毫秒)" align="center"></el-table-column>
        <el-table-column prop="success" label="是否成功" align="center">
          <template slot-scope="scope">
            <i class="el-icon-success" style="color: green; font-size: 18px;" v-if="scope.row.success"></i>
            <i class="el-icon-error" style="color: red; font-size: 18px;" v-else></i>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        hide-on-single-page
        :current-page.sync="tablePage"
        @current-change="loadUserLos">
      </el-pagination>
  </div>
</template>

<script>
  export default {
    name: "user-logs",
    props: {
      userUuid: String
    },
    data() {
      return {
        tableLoading: false,
        logs: [],
        tablePage: 1,
        total: 0,
        form: {
          optName: "",
          userUuid: "",
          beginTime: "",
          endTime: "",
          clientIp: "",
          success: ""
        },
        beginTimePickerOptions: {
          disabledDate: time => {
            if (this.form.endTime && time.getTime() > this.form.endTime.getTime()) return true
            return time.getTime() > new Date().getTime()
          }
        },
        endTimePickerOptions: {
          disabledDate: time => {
            if (this.form.beginTime && time.getTime() < this.form.beginTime.getTime()) return true
            return time.getTime() > new Date().getTime()
          }
        }
      }
    },
    mounted() {
      this.loadUserLos()
    },
    watch: {
      userUuid(){
        this.users = []
        this.tablePage = 1
        this.$refs.form.resetFields();
        this.loadUserLos()
      }
    },
    methods: {
      search() {
        this.tablePage = 1
        this.users = []
        this.loadUserLos();
      },
      loadUserLos() {
        this.form.userUuid = this.userUuid;
        this.tableLoading = true;
        let params = this.$utils.copy(this.form)
        if (params.beginTime) {
          params.beginTime = this.$moment(params.beginTime).format("YYYY-MM-DD");
        }
        if (params.endTime) {
          params.endTime = this.$moment(params.endTime).format("YYYY-MM-DD");
        }
        params.page = this.tablePage;
        this.$axios.get("/user/logs", {params}).then(res => {
          this.tableLoading = false;
          if (res.data.code == 0) {
            res.data.data.forEach(e => {
              if (e.beginTime) {
                e.beginTime = this.$moment(e.beginTime).format("YYYY-MM-DD HH:mm:ss");
              }
            });
            this.logs = res.data.data;
            this.total = res.data.total;
          } else {
            this.$message.error( res.data.msg ? res.data.msg : "系统异常")
          }
        }).catch(() => {
          this.$message.error("系统异常")
          this.tableLoading = false;
        });
      },
      reset() {
        this.$refs.form.resetFields()
        this.search()
      },
      close() {
        this.$emit('update:show', false)
      },
      closed() {
        this.users = []
        this.tablePage = 1
        this.$refs.form.resetFields()
      }
    }
  }

</script>

<style>
  .timePickIn .el-input__inner {
    width: 220px;
  }
</style>
