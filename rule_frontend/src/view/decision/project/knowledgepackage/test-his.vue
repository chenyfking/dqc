<template>
  <div>
    <el-button type="danger" icon="el-icon-time" @click="openHis" :loading="hisLoading">
      {{hisLoading ? '加载中...' : '历史测试数据'}}
    </el-button>

    <el-dialog title="历史测试数据" :visible.sync="showHisDialog" width="70%" append-to-body :close-on-click-modal="false">
      <el-tabs v-model="activeHisTab" type="border-card" v-if="hisData.length != 0"
               @tab-add="loadMore" closable :addable="addable" @tab-remove="delHisData" v-loading="pageLoading">
        <el-tab-pane :label="data.createTime" :name="data.uuid" v-for="data in hisData" :key="Math.random()">
          <div class="toolbar">
            <el-button type="primary" icon="el-icon-check" @click="chooseHistory(data)">选择</el-button>
          </div>
          <el-row :gutter="10">
            <el-col :span="6">
              <el-table :data="data.data" :row-style="{cursor: 'pointer'}"
                        :row-class-name="({row, rowIndex}) => row.index = rowIndex"
                        @row-click="(row) => testData.activeIndex = row.index"
                        highlight-current-row border>
                <el-table-column prop="name" label="数据结构" align="center"></el-table-column>
              </el-table>
            </el-col>
            <el-col :span="18">
              <el-table v-for="(fact, index) in data.data" :key="fact.id" v-show="index == testData.activeIndex"
                        :data="fact.fields" class="tb-edit" highlight-current-row border>
                <el-table-column label="值" align="center">
                  <template slot-scope="scope">
                          <span v-if="scope.row.type == 'Boolean'">
                              {{!scope.row.original ? '' : (scope.row.original === 'true' ? '是' : '否')}}
                          </span>
                    <span v-else>{{scope.row.original}}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="value" label="结果" align="center"></el-table-column>
                <el-table-column prop="label" label="标题" align="center"></el-table-column>
                <el-table-column prop="type" label="数据类型" align="center"></el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
      <p>{{hisData.length == 0 ? '暂无数据' :''}}</p>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "test-his",
    computed: {
      pkg() {
        return this.$store.state.pkg
      }
    },
    data() {
      return {
        hisLoading: false,
        showHisDialog: false,
        activeHisTab: '',
        hisData: [],
        testData: {
          activeIndex: 0,
          facts: null
        },
        page: 1,
        pageNum: 3,
        pageLoading: false,
        addable: true
      }
    },
    methods: {
      openHis() {
        this.hisLoading = true
        this.page = 1
        this.hisData = []
        this.loadHis()
      },
      loadHis() {
        this.$axios.get("/history", {
          params: {
            packageId: this.pkg.uuid,
            page: this.page,
            pageNum: this.pageNum,
            baselineVersion: this.pkg.baselineVersion,
          }
        }).then(res => {
          if (res.data.code == 0) {
            res.data.data.forEach((i, k) => {
              i.data = JSON.parse(i.data);
              i.createTime = this.$moment(i.createTime).format('YYYY-MM-DD HH:mm');
            })
            this.hisData.push(...res.data.data)
            this.showHisDialog = true;
            if (this.hisData.length > 0 && this.page == 1) {
              this.activeHisTab = this.hisData[0].uuid
            }
            if (res.data.data.length < this.pageNum) {
              this.addable = false
            }
          }
        }).catch(() => {
          this.hisLoading = false
          this.pageLoading = false
          this.addable = false
        }).then(() => {
          this.hisLoading = false
          this.pageLoading = false
        })
      },
      delHisData(uuid) {
        this.$axios.post("/history/delete", this.$qs.stringify({
          uuid: uuid,
        })).then(res => {
          if (res.data.code == 0) {
            let tabs = this.hisData
            let activeName = this.activeHisTab
            if (activeName === uuid) {
              tabs.forEach((tab, index) => {
                if (tab.uuid === uuid) {
                  let nextTab = tabs[index + 1] || tabs[index - 1]
                  if (nextTab) {
                    activeName = nextTab.uuid
                  }
                }
              })
            }
            this.activeHisTab = activeName
            let deleteIndex
            this.hisData.some((tab, i) => {
              if (tab.uuid == uuid) {
                deleteIndex = i
                return true
              }
            })
            this.$delete(this.hisData, deleteIndex)
          }
        }).catch(() => {
          this.$message.error( "操作失败")
        })
      },
      chooseHistory(data) {
        this.$emit('choose', data.data)
        this.showHisDialog = false;
      },
      loadMore() {
        this.page++
        this.pageLoading = true
        this.loadHis()
      }
    }
  }
</script>

<style scoped>

</style>
