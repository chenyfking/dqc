<template>
  <div>
    <div class="toolbar">
      <el-button type="danger" icon="el-icon-delete" @click="deleAll">清空回收站</el-button>
      <el-button
        :loading="saveLoading"
        @click="returnBack"
        icon="el-icon-success">
        {{saveLoading ? '还原中...' : '批量还原'}}
      </el-button>
      <el-button
        :loading="testLoading"
        @click="del"
        icon="el-icon-delete">
        {{testLoading ? '删除中...' : '批量删除'}}
      </el-button>
    </div>
    <el-table
      v-loading="tableLoading"
      element-loading-text="数据加载中..."
      @sort-change="sortChange"
      :data="recycleData"
      ref="table"
      @selection-change="checkSelect">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="assetsType" label="文件类型">
        <template slot-scope="scope">
          <span>
            {{$utils.getAssetsTypeText(scope.row.assetsType)}}{{scope.row.type == 'dir' ? '/文件夹' : ''}}{{(scope.row.type == 'tpl' ? '模板' : '')}}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="assetsName" label="文件名称"></el-table-column>
      <!-- <el-table-column prop="creator.realname" label="删除人"></el-table-column> -->
      <el-table-column prop="createTime" label="删除日期"></el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-popconfirm title="确定彻底删除？" @onConfirm="del(scope.row,false)">
            <el-button type="text" slot="reference">彻底删除</el-button>
          </el-popconfirm>
          <el-button type="text" @click="returnBack(scope.row,false)">还原</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      background
      layout="total, prev, pager, next,jumper"
      :total="total"
      hide-on-single-page
      :page-size="8"
      @current-change="toPage"
    ></el-pagination>
  </div>
</template>

<script>
export default {
  name: "index",
  data() {
    return {
      tableLoading: false,
      recycleData: [],
      tablePage: 1,
      total: 0,
      sortOption: {
        sortField: "",
        sortDirection: ""
      },
      tabSelect: [],
      testLoading: false,
      saveLoading: false
    };
  },
  computed: {
    isReload() {
      return this.$store.state.reloadRecyle
    }
  },
  watch: {
    isReload(newVal) {
      if (newVal) {
        this.initData()
      }
    }
  },
  mounted() {
    this.initData();
  },
  methods: {
    toPage(page) {
      this.tablePage = page;
      this.initData();
    },
    initData() {
      this.tableLoading = true;
      this.$axios
        .get("/recycle", {
          params: {
            page: this.tablePage,
            assetsType: this.assetsType,
            projectUuid: this.$route.params.uuid
          }
        })
        .then(res => {
          if (res.data.code == 0) {
            res.data.data.forEach(e => {
              if (e.createTime) {
                e.createTime = this.$moment(e.createTime).format(
                  "YYYY-MM-DD HH:mm:ss"
                );
              }
            });
            this.recycleData = res.data.data;
            this.total = res.data.total;
            this.$store.commit('setRecyle', false)
          }
        })
        .then(() => {
          this.tableLoading = false;
        });
    },
    checkSelect(data) {
      this.tabSelect = [];
      data.forEach(item => {
        this.tabSelect.push({
          uuid: item.uuid,
          type: item.type,
          assetsUuid: item.assetsUuid
        });
      });
    },
    del(value = [], isAll = true) {
      let length = this.tabSelect.length;
      if (!length && isAll) {
        this.$message.warning( "请选择要删除的文件")
        return;
      }
      let arr
      if (isAll && length) {
        arr = this.tabSelect
      } else {
        arr = [{
          uuid: value.uuid,
          type: value.type,
          assetsUuid: value.assetsUuid
        }]
      }
      this.$axios.post("/recycle/delete", this.$qs.stringify({
        deleteJson: JSON.stringify(arr)
      })).then(res => {
        this.loading = false;
        if (res.data.code == 0) {
          this.$message.success("删除成功")
          this.initData();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "删除失败")
        }
      }).catch(() => {
        this.$message.error("删除失败")
        this.loading = false;
      });
    },
    deleAll() {
      this.$confirm("此操作将永久删除文件, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$axios
          .post(
            "/recycle/empty",
            this.$qs.stringify({
              projectUuid: this.$route.params.uuid
            })
          )
          .then(res => {
            if (res.data.code == 0) {
              this.$message.success("清空成功")
              this.initData();
              this.$emit("delete");
            } else {
              this.$message.error( res.data.msg ? res.data.msg : "清空失败")
            }
          })
          .catch(() => {
           this.$message.error("清空失败")
          });
      });
    },
    returnBack(value = [], isAll = true) {
      let restoreData
      let length = this.tabSelect.length;
      if (isAll && length) {
        restoreData = this.tabSelect
      } else if (!isAll) {
        restoreData = [{
          uuid: value.uuid,
          type: value.type,
          assetsUuid: value.assetsUuid
        }]
      } else {
        this.$message.warning( "请选择要还原的文件")
        return;
      }
      this.$axios.post("/recycle/restore", this.$qs.stringify({
        restoreData: JSON.stringify(restoreData),
        projectUuid: this.$route.params.uuid
      })).then(res => {
        if (res.data.code == 0) {
          this.$message.success("还原成功")
          this.initData();
          this.$emit("reduction", res.data.data);
        } else {
          this.$message.error( res.data.msg ? res.data.msg : "还原失败")
        }
      }).catch(() => {
        this.$message.error( "还原失败")
      });
    },
    sortChange(data) {
      this.sortOption.sortField = data.prop;
      this.sortOption.sortDirection =
        data.order == "ascending" ? "asc" : "desc";
      this.initData();
    }
  }
};
</script>

<style scoped>
.note {
  margin: 0 0 20px;
  padding: 10px 30px 10px 15px;
  /*border-left: 5px solid #eee;*/
  width: 98%;
}

.note-success {
  background-color: transparent;
  /*border-left: 5px solid #fff !important;*/
  /*border-bottom:1px solid #fff;border-radius: 2px;*/
}

p.block {
  margin: 5px 0;
  color: #fff;
}
</style>
