<template>
  <div class="searchModelContent">
    <div class="searchVarContent">
      <el-input
        class="treeInput"
        placeholder="输入关键字"
        v-model.trim="searchInput"
        @keyup.enter.native="searchResources">
        <i slot="suffix" class="el-input__icon el-icon-search" @click="initData()"></i>
      </el-input>
      <el-select
        class="treeSelect"
        v-model="searchCondition"
        placeholder="请选择"
        @change="selectSearch">
        <el-option key="variable" label="搜变量" value="variable"></el-option>
        <el-option key="rule" label="搜规则" value="rule"></el-option>
      </el-select>
    </div>
    <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="searchList">
      <el-table-column prop="name" label="名称">
        <template slot-scope="scope">
          <el-button type="text" @click.stop="link(scope.row.uuid)">{{scope.row.name}}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型">
        <template slot-scope="scope">
          <span>{{$utils.getAssetsTypeText(scope.row.type)}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="操作">
        <template slot-scope="scope">
          <el-button type="text" @click.stop="link(scope.row.uuid)">打开</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: "index",
  props: {
    searchData: Object
  },
  watch: {
    searchData(newVale) {
      this.searchInput = this.searchData.searchInput;
      this.searchCondition = this.searchData.searchCondition;
      this.initData();
    }
  },
  data() {
    return {
      tableLoading: false,
      searchList: [],
      tablePage: 1,
      total: 0,
      searchInput: "",
      searchCondition: ""
    };
  },
  mounted() {
    this.searchInput = this.searchData.searchInput;
    this.searchCondition = this.searchData.searchCondition;
    this.initData();
  },
  methods: {
    toPage(page) {
      this.tablePage = page;
      this.initData();
    },
    submit() {
      this.initData();
    },
    initData() {
      if (!this.searchInput) return

      this.tableLoading = true;
      let params = {
        projectUuid: this.$route.params.uuid,
        keyword: this.searchInput,
        type: this.searchCondition
      };
      this.$axios.get("/assets/search", { params }).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          res.data.data.forEach(e => {
            if (e.begin_time) {
              e.begin_time = this.$moment(e.begin_time).format(
                "YYYY-MM-DD HH:mm:ss"
              );
            }
          });
          this.searchList = res.data.data;
        } else {
          this.$message.error( res.data.msg ? res.data.msg : "系统异常")
        }
      }).catch(() => {
        this.$message.error("系统异常")
        this.tableLoading = false;
      });
    },
    selectSearch(e) {
      this.initData();
    },
    searchResources() {
      this.initData();
    },
    link(uuid) {
      this.$emit("activateAssets", uuid);
    }
  }
};
</script>

<style >
.timePickIn .el-input__inner {
  width: 220px;
}
.searchModelContent .searchVarContent {
  display: flex;
  margin-top: 12px;
  width: 314px;
}
.searchModelContent .treeInput {
  margin-left: 4px;
  width: 60%;
  display: block;
}
.searchModelContent .treeSelect {
  width: 30%;
  margin-left: 10px;
}
</style>
