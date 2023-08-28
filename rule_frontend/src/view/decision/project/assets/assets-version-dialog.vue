<template>
  <el-dialog
    title="版本列表"
    :top="$dialogTop"
    :visible.sync="show"
    width="70%"
    append-to-body
    @open="open"
    @closed="$emit('update:active', false)">
    <div class="toolbar">
      <el-button type="primary" @click="openDiff" icon="el-icon-document-copy">对比差异</el-button>
    </div>
    <el-table
      :data="list"
      v-loading="tableLoading"
      element-loading-text="数据加载中..."
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="versionNo" label="版本号" align="center">
          <template slot-scope="scope">V{{scope.row.versionNo}}</template>
        </el-table-column>
      <el-table-column property="versionDesc" show-overflow-tooltip label="版本描述"></el-table-column>
      <!-- <el-table-column property="creator.realname" label="创建人"></el-table-column> -->
      <el-table-column property="createTime" label="创建时间" width="160">
        <template slot-scope="scope">{{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="viewAssets(scope.row)">查看</el-button>
          <el-popconfirm title="确定切换到当前版本？" @confirm="change(scope.row.versionNo)">
            <el-button type="text" slot="reference">切换</el-button>
          </el-popconfirm>
          <el-popconfirm title="确定删除当前版本？" @confirm="del(scope.row.versionNo)">
            <el-button type="text" slot="reference">删除</el-button>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div slot="footer">
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        hide-on-single-page
        @current-change="toPage">
      </el-pagination>
    </div>

    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show">
    </assets-view>
    <assets-diff
      :assets-uuid="diffAssets.uuid"
      :assets-version="diffAssets.version"
      :assets-type="diffAssets.type"
      :show.sync="diffAssets.show">
    </assets-diff>
  </el-dialog>
</template>

<script>
  import assetsDiff from './assets-diff'

  export default {
    name: "assets-version-dialog",
    props: {
      active: Boolean,
      assets: Object
    },
    components: {
      'assets-diff': assetsDiff
    },
    watch: {
      active(newVal) {
        this.show = newVal
      }
    },
    data() {
      return {
        show: this.active,
        list: [],
        tableLoading: false,
        total: 0,
        page: 1,
        viewingAssets: {
          uuid:"",
          version: 0,
          show: false
        },
        diffAssets: {
          uuid: '',
          type: '',
          version: [],
          show: false
        },
        tabSelect: []
      }
    },
    methods: {
      open() {
        this.initVersion()
      },
      toPage(p) {
        this.page = p;
        this.initVersion();
      },
      initVersion() {
        this.tableLoading = true
        this.$axios.get("/assets/version/list", {
          params: {
            uuid: this.assets.uuid,
            page: this.page
          }
        }).then(res => {
          if (res.data.code == 0) {
            this.total = res.data.total
            if (res.data.total > 0) {
              this.list = res.data.data;
            } else {
              this.list = []
            }
            this.show = true;
          }
          this.tableLoading = false
        }).catch(() => {
          this.tableLoading = false
        })
      },
      change(versionNo) {
        this.tableLoading = true
        this.$axios.post("/assets/version/change", this.$qs.stringify({
          uuid: this.assets.uuid,
          versionNo: versionNo
        })).then((res) => {
          this.tableLoading = false
          if (res.data.code == 0) {
            this.show = false;
            this.$emit('change', res.data.data.content)
            this.$message.success('版本切换成功')
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '版本切换失败')
          }
        }).catch(() => {
          this.$message.error('版本切换失败')
          this.tableLoading = false
        })
      },
      del(versionNo) {
        this.tableLoading = true
        this.$axios.post("/assets/version/delete", this.$qs.stringify({
          uuid: this.assets.uuid,
          versionNo: versionNo
        })).then(res => {
          this.tableLoading = false
          if (res.data.code == 0) {
            this.$message.success('删除成功')
            this.initVersion();
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '删除失败')
          }
        }).catch(() => {
          this.$message.error('删除失败')
          this.tableLoading = false
        })
      },
      viewAssets(row) {
        this.viewingAssets.uuid = row.uuid
        this.viewingAssets.version = row.versionNo
        this.viewingAssets.show = true
      },
      openDiff() {
        if (this.tabSelect.length == 2) {
          this.diffAssets.uuid = this.assets.uuid
          this.diffAssets.type = this.assets.type
          this.diffAssets.version = this.tabSelect
          this.diffAssets.show = true
        } else {
          this.$message.warning( "请勾选两个版本对比")
        }
      },
      //选中对比
      handleSelectionChange(data) {
        this.tabSelect = [];
          data.forEach(item => {
            this.tabSelect.push(item);
          });
      },
    }
  }
</script>

<style scoped>

</style>
