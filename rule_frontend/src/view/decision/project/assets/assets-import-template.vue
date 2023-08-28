<template>
  <div class="referContent">
    <!-- <el-button
      icon="iconfont iconVersionupdaterule-copy"
      @click="initData"
      :loading="loading"
      v-if="$hasPerm('project.asset:edit')">
      {{loading ? '加载中...' : '导入模板'}}
    </el-button> -->

    <el-dialog
      title="模板列表"
      :top="$dialogTop"
      :visible.sync="show"
      :close-on-click-modal="false"
      class="referContent"
      v-if="show"
      append-to-body>
      <el-table
        class="referTable tb-edit"
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        :data="searchList"
        highlight-current-row>
        <el-table-column prop="label" label="文件名称" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="文件类型">
          <template slot-scope="scope">
            <span>{{$utils.getAssetsTypeText(scope.row.type)}}</span>
          </template>
        </el-table-column>
        <el-table-column label="添加时间">
          <template slot-scope="scope">{{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}</template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" @click="viewAssets(scope.row)">查看</el-button>
            <el-button type="text" @click="importTpl(scope.row)">导入</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
      </div>
    </el-dialog>
    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show"
      template>
    </assets-view>
  </div>
</template>

<script>
export default {
  name: "refer-btn",
  props: {
    assets: Object
  },
  data() {
    return {
      loading: false,
      show: false,
      tableLoading: false,
      searchList: [],
      tablePage: 1,
      total: 0,
      viewingAssets: {
        uuid: "",
        version: 0,
        show: false
      }
    };
  },
  mounted() {},
  methods: {
    initData() {
      this.loading = true;
      const rul = "/assets/tree";
      const type = `tpl_${this.assets.type}`;
      this.$axios
        .get(rul, {
          params: {
            projectUuid: this.$route.params.uuid,
            type
          }
        })
        .then(res => {
          if (res.data.code == 0) {
            res.data.data.forEach(item => {
              item.type = item.type.replace("tpl_", "");
            });
            this.searchList = res.data.data;
            this.show = true;
          } else {
            this.$message.error("查询失败");
          }
        })
        .catch(() => {
          this.$message.error("查询失败");
        })
        .then(() => {
          this.loading = false;
        });
    },
    cellClick(row, column, cell, event) {
      const el = this.$refs["input_original_" + row.uuid][0];
      if (column.label == "版本号" && el) {
        this.$nextTick(function() {
          el.toggleMenu();
        });
      }
    },
    handleClose() {
      this.searchList = [];
    },
    toPage(page) {
      this.tablePage = page;
      this.initData();
    },
    viewAssets(row) {
      this.viewingAssets.uuid = row.uuid;
      this.viewingAssets.version = row.select;
      this.viewingAssets.show = true;
    },
    importTpl(row) {
      this.$axios.get("/template/" + row.uuid).then(res => {
        if (res.data.code == 0) {
          this.$emit("importTpl", res.data.data.content);
          this.show = false;
          this.$message.success("导入成功");
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "导入失败");
        }
      }).catch(() => {
        this.$message.error("导入失败");
      });
    }
  }
};
</script>

<style scoped>
</style>
