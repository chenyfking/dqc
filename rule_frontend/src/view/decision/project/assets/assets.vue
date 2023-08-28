<template>
  <el-container v-loading="projectName == ''" element-loading-text="数据加载中...">
    <el-aside ref="aside" class="aside-wrap" width="250px" v-show="projectName != ''">
      <div class="search-wrap">
        <el-input
          style="width: 100%;"
          size="mini"
          placeholder="输入关键字进行过滤"
          v-model="filterText">
          <i slot="suffix" class="el-input__icon el-icon-search" @click="searchResources"></i>
        </el-input>
      </div>
      <div class="tree-wrap">
        <el-tree
          :props="treeProps"
          node-key="uuid"
          ref="MenuTree"
          draggable
          :allow-drop="allowDrop"
          :allow-drag="allowDrag"
          :filter-node-method="filterNode"
          :default-expanded-keys="defaultExpend"
          highlight-current
          @node-click="handleNodeClick"
          @node-drop="nodeDrop"
          :load="loadTreeNode"
          lazy>
          <span class="custom-tree-node" slot-scope="{ node, data }">
            <treeMenu
              :menuNode="node"
              :menuData="data"
              @handleCommand="handleCommand"
              @importFact="importFact">
            </treeMenu>
          </span>
        </el-tree>
      </div>
    </el-aside>

    <el-main ref="main" v-loading="assetsLoading" element-loading-text="数据加载中..." v-show="projectName != ''">
      <div class="header-wrap">
        <el-page-header @back="() => $router.push('/decision/project')" :content="projectName"></el-page-header>
        <div v-if="$hasPerm('project.asset:add')" style="margin-top: -5px;">
          <el-upload
            style="display: inline-block;"
            action
            :on-change="importAssets"
            :auto-upload="false"
            :disabled="importLoading"
            :show-file-list="false"
            accept=".rule"
            class="upload">
          </el-upload>
          <dropdown @command="add">
            <el-button type="primary" icon="el-icon-plus">新建文件</el-button>
            <dropdown-menu slot="dropdown" style="width: 170px">
              <dropdown-item
                v-for="(e, i) in $utils.assetsArr"
                v-if="e.add"
                :key="i"
                :command="e.type"
                :icon="e.icon">
                {{e.name}}
              </dropdown-item>
            </dropdown-menu>
          </dropdown>
        </div>
      </div>

      <el-tabs
        v-model="activeTab"
        type="border-card"
        closable
        v-show="assetsArr.length > 0"
        ref="tabs"
        @tab-remove="removeTab">
        <el-tab-pane v-for="assets in assetsArr" :key="assets.uuid" :name="assets.uuid">
          <span slot="label">
            <i class="el-icon-lock" v-if="assets.locked"></i>
            <i :class="$utils.getAssetsTypeIcon(assets.type)"></i>
            {{assets.name}}
          </span>
          <fact-assets
            v-if="assets.type == 'fact'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets">
          </fact-assets>
          <constant-assets
            v-else-if="assets.type == 'constant'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets">
          </constant-assets>
          <guidedrule-assets
            v-else-if="assets.type == 'guidedrule'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets"
            @addTemplate="addTemplate">
          </guidedrule-assets>
          <rulescript-assets
            v-else-if="assets.type == 'rulescript'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets">
          </rulescript-assets>
          <ruletable-assets
            v-else-if="assets.type == 'ruletable'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets"
            @addTemplate="addTemplate">
          </ruletable-assets>
          <ruletree-assets
            v-else-if="assets.type == 'ruletree'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets"
            @addTemplate="addTemplate">
          </ruletree-assets>
          <scorecard-assets
            v-else-if="assets.type == 'scorecard'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets"
            @addTemplate="addTemplate">
          </scorecard-assets>
          <ruleflow-assets
            v-else-if="assets.type == 'ruleflow'"
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets">
          </ruleflow-assets>
          <testcase
            :assets="assets"
            :readonly="isAssetsReadonly(assets)"
            @refresh="refreshAssets"
            @delete="deleteAssets"
            v-else-if="assets.type == 'testcase'">
          </testcase>
          <pkg v-else-if="assets.type == 'pkg'"></pkg>
          <searchResult
            v-else-if="assets.type == 'search'"
            :searchData="assets.searchVal"
            @activateAssets="activateAssets">
          </searchResult>
          <recycle v-else-if="assets.type == 'recycle'" ref="recycledom" @reduction="reduction"></recycle>
        </el-tab-pane>
      </el-tabs>

      <el-dialog
        :title="'新建'+newFile.typeStr"
        :visible.sync="showAddFileDialog"
        @opened="$refs.newFileName.focus()"
        @closed="$refs.newFileForm.resetFields();clearDir()"
        width="30%"
        :close-on-click-modal="false">
        <el-form
          :model="newFile"
          :rules="newFileRules"
          ref="newFileForm"
          label-width="80px"
          @submit.native.prevent>
          <el-form-item label="名称" prop="name">
            <el-input
              v-model.trim="newFile.name"
              ref="newFileName"
              :maxlength="20"
              show-word-limit
              @keyup.enter.native="addSave"
              placeholder="请输入名称">
            </el-input>
          </el-form-item>
          <el-form-item label="描述" prop="description" v-if="newFile.type != 'dir'">
            <el-input
              v-model="newFile.description"
              type="textarea"
              placeholder="请输入描述"
              :autosize="{ minRows: 6 }"
              :maxlength="100"
              show-word-limit>
            </el-input>
          </el-form-item>
          <el-form-item label="文件夹" prop="dir" :class="newFile.type == 'dir' ? 'is-required' : ''">
            <el-input
              v-model.trim="newFile.dir"
              readonly
              @keyup.enter.native="addSave"
              @focus="selectDir"
              placeholder="请选择文件夹">
              <el-button slot="append" icon="el-icon-delete" @click="clearDir"></el-button>
            </el-input>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="showAddFileDialog = false" icon="el-icon-error">取 消</el-button>
          <el-button
            type="primary"
            @click="addSave"
            icon="el-icon-success"
            :loading="addLoading">
            {{addLoading ? '提交中...' : '确 定'}}
          </el-button>
        </div>

        <el-dialog
          title="选择文件夹"
          width="30%"
          :visible.sync="showDirDialog"
          append-to-body
          :close-on-click-modal="false">
          <el-tree
            :props="defaultProps"
            v-if="showDirDialog"
            ref="dirTree"
            :load="loadSelectTreeNode"
            lazy
            node-key="uuid"
            @node-click="handleNodeSelectClick">
          </el-tree>

          <div slot="footer">
            <el-button @click="showDirDialog = false" icon="el-icon-error">取 消</el-button>
            <el-button type="primary" @click="confirmSelectDir" icon="el-icon-success">确 定</el-button>
          </div>
        </el-dialog>
      </el-dialog>

      <el-dialog
        title="选择要移动文件夹"
        width="30%"
        :visible.sync="showDirMoveDialog"
        append-to-body
        :close-on-click-modal="false">
        <el-tree ref="dirMoveTree" :data="dirsMove" :default-expand-all="true" node-key="id"></el-tree>
        <div slot="footer">
          <el-button @click="showDirMoveDialog = false" icon="el-icon-error">取 消</el-button>
          <el-button type="primary" @click="confirmSelectDirMove" icon="el-icon-success">确 定</el-button>
        </div>
      </el-dialog>
      <el-dialog
        title="编辑文件夹"
        :visible.sync="showEditFolderDialog"
        width="30%"
        @opened="$refs.newFolderName.focus()">
        <el-form
          :model="folder"
          ref="editFolderForm"
          :rules="newFileRules"
          label-width="80px"
          @submit.native.prevent>
          <el-form-item label="名称" prop="name">
            <el-input
              v-model="folder.name"
              ref="newFolderName"
              :maxlength="20"
              :show-word-limit="true"
              @keyup.enter.native="submitEditFolder"
              placeholder="请输入名称">
            </el-input>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="showEditFolderDialog = false" icon="el-icon-error">取 消</el-button>
          <el-button
            type="primary"
            @click="submitEditFolder"
            icon="el-icon-success"
            :loading="editLoading">
            {{editLoading ? '提交中...' : '确 定'}}
          </el-button>
        </div>
      </el-dialog>

      <ul
        class="el-dropdown-menu el-popper"
        ref="contextMenu"
        style="top: -100px; transform-origin: center bottom 0px;">
        <li class="el-dropdown-menu__item" @click="closeAll">关闭所有标签页</li>
        <li class="el-dropdown-menu__item" ref="contextMenu-closeOther" @click="closeOther">关闭其它标签页</li>
      </ul>
    </el-main>
  </el-container>
</template>

<script>
import fact from "../fact";
import constant from "../constant";
import guidedrule from "../guidedrule";
import ruletable from "../ruletable";
import ruletree from "../ruletree";
import scorecard from "../scorecard";
import ruleflow from "../ruleflow";
import rulescript from "../rulescript";
import searchResult from "../searchResult";
import pkg from "../knowledgepackage/knowledgepackage";
import testcase from "../testcase";
import recycle from "../recycle";
import treeMenu from "./treeMenu";

export default {
  name: "assets",
  components: {
    "fact-assets": fact,
    "constant-assets": constant,
    "guidedrule-assets": guidedrule,
    "ruletable-assets": ruletable,
    "ruletree-assets": ruletree,
    "scorecard-assets": scorecard,
    "ruleflow-assets": ruleflow,
    "rulescript-assets": rulescript,
    pkg,
    searchResult,
    treeMenu,
    testcase,
    recycle,
  },
  watch: {
    filterText(val) {
      this.$refs.MenuTree.filter(val);
    },
    activeTab(val) {
      this.$refs.MenuTree.setCurrentKey(val);
    },
    assetsLink(newVal, oldVal) {
      this.activateAssets(newVal.uuid);
    },
  },
  computed: {
    assetsLink() {
      return this.$store.state.assetsLink;
    }
  },
  data() {
    const self = this;
    return {
      defaultProps: {
        children: "children",
        label: "label",
      },
      treeProps: {
        isLeaf: "leaf",
      },
      filterText: "",
      highlightCurrent: true,
      expandedKey: [],
      defaultExpend: ["setVariable", "ruleConfiguration"],
      assetsGroup: [
        {
          id: "setVariable",
          uuid: "setVariable",
          label: "设置变量",
          icon: "el-icon-setting",
          children: [
            {
              id: "fact",
              uuid: "fact",
              type: "menu-fact",
              label: "数据结构",
              icon: "iconfont iconshujumoxing",
              children: [],
              leaf: false,
            },
            {
              id: "constant",
              uuid: "constant",
              type: "menu-constant",
              label: "枚举常量",
              icon: "iconfont iconxinzengchangliang",
              children: [],
              leaf: false,
            },
          ],
        },
        {
          id: "ruleConfiguration",
          uuid: "ruleConfiguration",
          label: "规则配置",
          icon: "iconfont iconguize",
          children: [
            {
              id: "guidedrule",
              uuid: "guidedrule",
              type: "menu-guidedrule",
              label: "向导式决策集",
              icon: "iconfont iconweibiaoti-",
              children: [],
              leaf: false,
            }
          ],
        },
        // {
        //   id: "template",
        //   uuid: "template",
        //   label: "规则模板",
        //   icon: "el-icon-document-copy",
        //   children: [
        //     {
        //       id: "menu-tpl_guidedrule",
        //       uuid: "menu-tpl_guidedrule",
        //       type: "menu-tpl_guidedrule",
        //       label: "向导式决策集模板",
        //       icon: "iconfont iconweibiaoti-",
        //       children: [],
        //       leaf: false,
        //     }
        //   ],
        // },
        {
          id: "modelPackaging",
          uuid: "modelPackaging",
          label: "知识包配置",
          icon: "el-icon-suitcase",
          children: [
            {
              id: "pkg",
              uuid: "pkg",
              label: "知识包",
              type: "pkg",
              leaf: true,
            },
          ],
        },
        {
          id: "recycle",
          uuid: "recycle",
          label: "回收站",
          icon: "el-icon-delete",
          leaf: true,
        },
      ],
      treeAssetsGroup: [],
      dirs: [],
      newFile: {
        type: "",
        name: "",
        description: "",
        tag: "",
        typeStr: "",
        dirParentId: "",
        dir: "",
        parentType: "",
      },
      newFileRules: {
        name: [
          { required: true, message: "请输入名称", trigger: "blur" },
          { max: 20, message: "长度不超过20个字符", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              if (
                value
                && (self.newFile.type == 'fact' || self.newFile.type == 'constant')
                && (value.indexOf('.') != -1 || value.indexOf('_') != -1)
              ) {
                callback(new Error("不能包含字符[.]或[_]"));
              } else {
                callback();
              }
            },
          }
        ],
        dir: [
          {
            validator: function (rule, value, callback) {
              if (!value && self.newFile.type == "dir") {
                callback(new Error("请选择文件夹"));
              } else {
                callback();
              }
            },
          },
        ],
      },
      showAddFileDialog: false,
      activeTab: "",
      assetsArr: [],
      tagFilters: [],
      rightClickTab: "",
      folder: {
        id: "",
        name: "",
      },
      showDirDialog: false,
      dirs: [],
      addLoading: false,
      showEditFolderDialog: false,
      editLoading: false,
      assetsLoading: false,
      showDirMoveDialog: false,
      dirsMove: [],
      moveNode: {},
      projectName: "",
      Allmessage: null,
      searchCondition: "variable",
      searchInput: "",
      node_had: null,
      resolve_had: null,
      dirsSelect: {},
      importLoading: false
    };
  },
  mounted() {
    this.init();
  },
  beforeDestroy() {
    this.$refs.main.$el.removeEventListener("scroll", this.pin);
    window.removeEventListener("resize", this.resizePin);
    document.removeEventListener("click", this.hideContextMenu);
  },
  methods: {
    init() {
      this.$axios.get('/project/' + this.$route.params.uuid).then(res => {
        if (res.data.code == 0) {
          this.projectName = res.data.data.name
          this.loadAssetsGroup()
          this.loadBoms()
          this.bindEvent()
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '系统繁忙，请稍候再试')
          this.$router.push('/decision/project')
        }
      }).catch(e => {
        this.$message.error('系统繁忙，请稍候再试')
        this.$router.push('/decision/project')
      })
    },
    loadBoms() {
      this.$store.dispatch('loadBoms', {
        projectUuid: this.$route.params.uuid,
        callback: () => {
          if (this.$route.params.assetsUuid) {
            this.activateAssets(this.$route.params.assetsUuid)
          } else if (this.$route.name == 'decisionKnowledgePackage') {
            this.activateAssets('pkg')
          }
        }
      })
    },
    bindEvent() {
      this.$refs.main.$el.addEventListener("scroll", this.pin);
      window.addEventListener("resize", this.resizePin);
      document.querySelector(".el-tabs__header").addEventListener("contextmenu", this.showContextMenu);
      document.addEventListener("click", this.hideContextMenu);
      window.addEventListener("unload", this.unlockEditOnunload);
    },
    unbindEvent() {
      this.$refs.main.$el.removeEventListener("scroll", this.pin);
      window.removeEventListener("resize", this.resizePin);
      document.querySelector(".el-tabs__header").removeEventListener("contextmenu", this.showContextMenu);
      document.removeEventListener("click", this.hideContextMenu);
      window.removeEventListener("unload", this.unlockEditOnunload);
    },
    add(type) {
      this.newFile.type = type;
      this.newFile.typeStr = this.$utils.getAssetsTypeText(type);
      this.showAddFileDialog = true;
    },
    closeAddDialog() {
      this.$refs.newFileForm.resetFields()
      this.clearDir()
    },
    addSave() {
      this.$refs.newFileForm.validate((valid) => {
        if (valid) {
          this.addLoading = true;
          if (this.newFile.type != "dir") {
            this.$axios.post("/assets/add", this.$qs.stringify({
              projectUuid: this.$route.params.uuid,
              name: this.newFile.name,
              description: this.newFile.description,
              type: this.newFile.type,
              dirParentId: this.newFile.dirParentId,
            })).then((res) => {
              this.addLoading = false;
              if (res.data.code == 0) {
                this.showAddFileDialog = false;
                this.$message.success("新建成功");
                this.newFile.uuid = res.data.data;
                this.newFile.leaf = true;
                this.newFile.dirParentId = this.newFile.dirParentId ? this.newFile.dirParentId : this.newFile.type;
                this.appendTree(this.newFile);
                this.$refs.newFileForm.resetFields();
                this.clearDir();
              } else {
                this.$message.error(res.data.msg ? res.data.msg : "新建失败");
              }
            }).catch(e => {
              console.error(e)
              this.$message.error("新建失败");
              this.addLoading = false;
            });
          } else {
            this.$axios.post("/folder", this.$qs.stringify({
              dirId: this.newFile.level,
              dirName: this.newFile.name,
              parentId: this.newFile.dirParentId,
              categoryName: this.newFile.parentType,
              projectUuid: this.$route.params.uuid,
            })).then(res => {
              this.addLoading = false;
              if (res.data.code == 0) {
                this.showAddFileDialog = false;
                this.$message.success("新建成功");
                this.newFile.uuid = res.data.data;
                this.newFile.leaf = false;
                this.newFile.type = this.newFile.parentType
                this.newFile.dirParentId = this.newFile.dirParentId ? this.newFile.dirParentId : this.newFile.type;
                this.appendTree(this.newFile);
                this.$refs.newFileForm.resetFields();
                this.clearDir();
              } else {
                this.$message.error(res.data.msg ? res.data.msg : "新建失败");
              }
            }).catch(() => {
              this.$message.error("新建失败");
              this.addLoading = false;
            });
          }
        }
      });
    },
    appendTree(newFile, isTemplate = false, isOpen = true) {
      let parentId = newFile.dirParentId
      if (isTemplate) {
        parentId = 'menu-' + parentId
      }
      const parent = this.$refs.MenuTree.getNode(parentId);
      const leaf = newFile.leaf ? true : false;
      const data = {
        uuid: newFile.uuid,
        type: newFile.type,
        label: newFile.name,
        leaf: leaf
      };
      if (parent && parent.loaded && parentId) {
        this.$refs.MenuTree.append(data, parent);
      }
      if (leaf && isOpen) {
        this.activateAssets(newFile.uuid, isTemplate);
      }
    },
    loadTreeNode(node, resolve) {
      if (node.level === 0) {
        this.node_had = node;
        this.resolve_had = resolve;
        return resolve(this.treeAssetsGroup);
      }
      let type = "";
      let parentUuid = "";
      if (node.level === 1 && node.data.id == "testcase") {
        type = "testcase";
      } else if (node.level === 1 && node.data.id == "recycle") {
        return resolve([]);
      } else if (node.level === 1) {
        return resolve(node.data.children);
      }

      if (node.level === 2 && node.data.leaf) {
        const modelPackaging = node.parent.data.id == "modelPackaging";
        const testcase = node.parent.data.id == "testcase";
        if (modelPackaging || testcase) {
          return resolve([]);
        }
      } else if (node.level === 2 && !node.data.leaf) {
        type =
          node.data.type.indexOf("menu-") != -1
            ? node.data.type.substring(5)
            : node.data.type;
      } else if (node.level > 2) {
        type = node.data.type;
        parentUuid = node.data.uuid;
      }
      this.$axios.get("/assets/tree", {
        params: {
          projectUuid: this.$route.params.uuid,
          type,
          parentUuid,
        }
      }).then((res) => {
        if (res.data.code == 0) {
          return resolve(res.data.data);
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "获取失败");
        }
      }).catch(() => {
        this.$message.error("获取失败");
      })
    },
    loadAssetsGroup() {
      var assetsGroup = JSON.stringify(this.assetsGroup);
      this.treeAssetsGroup = JSON.parse(assetsGroup);
      this.node_had.childNodes = [];
      this.loadTreeNode(this.node_had, this.resolve_had);
      this.$nextTick(() => {
        this.defaultExpend.forEach((item) => {
          this.$refs.MenuTree.store.nodesMap[item].expanded = true;
        });
      });

      this.setCurrentTab();
    },
    deleteAssets(uuid) {
      this.removeTab(uuid);
      const deleData = this.$refs.MenuTree.getNode(uuid);
      if (deleData) {
        this.$refs.MenuTree.remove(deleData);
      }
    },
    //菜单变更引起的初始化
    setCurrentTab() {
      if (this.assetsArr.length) {
        this.assetsArr.forEach((item) => {
          if (this.activeTab == item.uuid) {
            this.expandedKey = [];
            this.expandedKey.push(item.uuid);
            this.$nextTick(() => {
              this.$refs.MenuTree.setCurrentKey(item.uuid);
            })
          }
        });
      }
    },
    removeTab(targetName) {
      if (!targetName) return;
      let tabs = this.assetsArr;
      let activeName = this.activeTab;
      let index = -1
      tabs.some((tab, i) => {
        if (tab.uuid === targetName) {
          if (activeName === targetName) {
            let nextTab = tabs[i + 1] || tabs[i - 1];
            if (nextTab) {
              activeName = nextTab.uuid;
            }
          }
          index = i;
          return true;
        }
      });
      if (index != -1) {
        this.unlockEdit([tabs[index]])
        this.assetsArr.splice(index, 1)
      }

      this.activeTab = activeName;
    },
    activateAssets(uuid, isTemplate = false) {
      let exist;
      this.assetsArr.some((e) => {
        if (e.uuid == uuid) {
          exist = true;
          return true;
        }
      });
      if (exist) {
        this.activeTab = uuid;
        return
      }

      if (!isTemplate) {
        if (uuid != "pkg" && uuid != "search") {
          this.assetsLoading = true;
          this.$axios.get("/assets/" + uuid).then(res => {
            this.assetsLoading = false
            if (res.data.code == 0 && !this.$utils.isBlank(res.data.data)) {
              const assets = res.data.data;
              this.expandedKey = [assets.uuid];
              this.pushAssetsArr(assets)
              this.$nextTick(() => {
                this.$refs.MenuTree.setCurrentKey(assets.uuid)
                this.activeTab = uuid;
              })
              this.lockEdit(assets)
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "查询失败");
            }
          }).catch(e => {
            console.error(e)
            this.$message.error("查询失败");
            this.assetsLoading = false
          })
        } else {
          this.assetsArr.push({
            uuid: "pkg",
            type: "pkg",
            id: "pkg",
            name: "知识包",
          });
          this.expandedKey = ["pkg"];
          this.$nextTick(() => {
            this.$refs.MenuTree.setCurrentKey("pkg");
            this.activeTab = uuid;
          })
        }
      } else if (isTemplate) {
        this.assetsLoading = true;
        this.$axios.get("/template/" + uuid).then(res => {
          this.assetsLoading = false
          if (res.data.code == 0) {
            res.data.data.isTemplate = true;
            this.pushAssetsArr(res.data.data)
            this.$nextTick(() => {
              this.$refs.MenuTree.setCurrentKey(res.data.data.uuid);
              this.activeTab = uuid;
            });
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "查询失败");
          }
        }).catch(() => {
          this.$message.error("查询失败");
          this.assetsLoading = false;
        });
      }
    },
    pushAssetsArr(assets) {
      let exists
      this.assetsArr.some(e => {
        if (e.uuid == assets.uuid) {
          exists = true
          return true
        }
      })
      if (!exists) {
        this.assetsArr.push(assets)
      }
    },
    pin() {
      const pin = document.querySelectorAll(".pin");
      var scrollTop = this.$refs.main.$el.pageYOffset || this.$refs.main.$el.scrollTop;
      if (scrollTop > 140) {
        const width = this.$refs.tabs.$el.offsetWidth - 2 + "px";
        const left = this.$refs.aside.$el.offsetWidth + 20 + "px";
        pin.forEach(e => {
          if (e.className.indexOf("pin-wrap") == -1) {
            if (e.offsetHeight > 0) {
              e.parentNode.style.height = e.offsetHeight + "px";
            }
            e.className = "pin pin-wrap el-card is-always-shadow el-card__body";
            e.style.width = width;
            e.style.left = left;
            e.style.top = '0px';
          }
        });
      } else {
        pin.forEach(e => e.className = "pin");
      }
    },
    resizePin() {
      const width = this.$refs.tabs.$el.offsetWidth - 2 + "px";
      document.querySelectorAll(".pin").forEach(e => e.style.width = width);
    },
    showContextMenu(e) {
      const tab = e.target.closest('[id^="tab-"]');
      if (tab != null) {
        this.rightClickTab = tab.id.substring(4);
        this.$refs["contextMenu-closeOther"].style.display = "block";
      } else {
        this.$refs["contextMenu-closeOther"].style.display = "none";
      }
      const contextMenu = this.$refs.contextMenu;
      let x,
        y = e.y;
      if (e.x + contextMenu.offsetWidth > window.innerWidth) {
        x = e.x - contextMenu.offsetWidth;
      } else {
        x = e.x;
      }
      contextMenu.style.left = x + "px";
      contextMenu.style.top = y + "px";

      e.preventDefault();
      return false;
    },
    hideContextMenu(e) {
      if (this.$refs.contextMenu) {
        this.$refs.contextMenu.style.top = "-100px";
      }
    },
    closeAll() {
      this.activeTab = "";
      const assetsArr = this.assetsArr;
      this.assetsArr = [];
      this.unlockEdit(assetsArr);
    },
    closeOther() {
      this.activeTab = this.rightClickTab;
      const otherTabs = this.assetsArr.filter(
        tab => tab.uuid != this.rightClickTab
      );
      this.assetsArr = this.assetsArr.filter(
        tab => tab.uuid == this.rightClickTab
      );
      this.unlockEdit(otherTabs);
    },
    selectDir() {
      //新建资源文件下文件夹
      this.dirs = [];
      this.dirsSelect = {};
      if (this.newFile.type != "dir") {
        let type = this.newFile.type;
        this.dirs = [
          {
            id: type,
            uuid: type,
            type: type,
            label: this.$utils.getAssetsTypeText(type),
            children: [],
            leaf: false,
            level: 0,
          },
        ];
      } else {
        let dirs = [];
        let groupArr = this.assetsGroup[0].children.concat(
          this.assetsGroup[1].children
        );
        groupArr.forEach((e) => {
          dirs.push({
            id: e.id,
            uuid: e.uuid,
            type: e.uuid,
            label: this.$utils.getAssetsTypeText(e.id),
            children: [],
            leaf: false,
            level: 0,
          });
        });
        this.dirs = dirs;
      }
      this.showDirDialog = true;
    },
    handleNodeSelectClick(node) {
      this.dirsSelect = node;
    },
    loadSelectTreeNode(node, resolve) {
      if (node.level === 0) {
        return resolve(this.dirs);
      } else if (node.level == 1) {
        this.$axios.get("/assets/folderTree", {
          params: {
            projectUuid: this.$route.params.uuid,
            type: node.data.uuid,
          },
        }).then((res) => {
          if (res.data.code == 0) {
            return resolve(res.data.data);
          }
        });
      } else {
        if (node.data.children && node.data.children.length > 0) {
          return resolve(node.data.children);
        }
        return resolve([]);
      }
    },
    confirmSelectDir() {
      const node = this.dirsSelect;
      if (node == null) {
        this.$message.warning("请选择文件夹");
      } else {
        this.newFile.dir = node.label;
        this.newFile.parentType = node.type;
        this.newFile.dirParentId = node.level == 0 ? 0 : node.uuid;
        this.newFile.level = node.level + 1;
        this.showDirDialog = false;
      }
    },
    clearDir() {
      this.newFile.dir = "";
      this.newFile.dirParentId = "";
    },
    moveNodeFolder(node) {
      let dirsMove = [];
      this.moveNode = node;
      this.$axios.get("/assets/folderTree", {
        params: {
          projectUuid: this.$route.params.uuid,
          type: node.type,
        },
      }).then(res => {
        if (res.data.code == 0) {
          if (node.type == "constant" || node.type == "fact") {
            this.assetsGroup[0].children.some((e) => {
              if (node.type == e.id) {
                dirsMove.push({
                  id: 0,
                  label: this.$utils.getAssetsTypeText(e.id),
                  children: res.data.data,
                  uuid: node.type
                });
              }
            });
          } else {
            this.assetsGroup[1].children.some(e => {
              if (node.type == e.id) {
                dirsMove.push({
                  id: 0,
                  label: this.$utils.getAssetsTypeText(e.id),
                  children: res.data.data,
                  uuid: node.type,
                });
              }
            });
          }
          this.dirsMove = dirsMove;
          this.showDirMoveDialog = true;
        }
      });
    },
    copyNodeAction(node) {
      this.$axios.post("/assets/copy", this.$qs.stringify({
        name: node.label,
        uuid: node.uuid,
        projectUuid: this.$route.params.uuid,
        nowVersion: node.nowVersion,
        type: node.type
      })).then(res => {
        if (res.data.code == 0) {
          const data = {
            uuid: res.data.data.uuid,
            type: node.type,
            name: res.data.data.name,
            leaf: true,
            dirParentId: res.data.data.dirParentId == "0" ? node.type : res.data.data.dirParentId
          };
          this.appendTree(data);
          this.$message.success("复制成功");
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "复制失败");
        }
      }).catch(() => {
        this.$message.error("复制失败");
      });
    },
    // 提交移动文件夹
    confirmSelectDirMove() {
      const node = this.$refs.dirMoveTree.getCurrentNode();
      if (node == null) {
        this.$message.warning("请选择文件夹");
      } else {
        this.$axios.post("/assets/edit", this.$qs.stringify({
          uuid: this.moveNode.uuid,
          dirParentId: node.uuid,
        })).then(res => {
          if (res.data.code == 0) {
            this.$message.success("操作成功");
            const data = {
              name: this.moveNode.label,
              uuid: this.moveNode.uuid,
              dirParentId: node.uuid,
              type: this.moveNode.type,
              leaf: true
            };
            this.deleteAssets(this.moveNode.uuid);
            this.appendTree(data);
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "操作失败");
          }
        }).catch(() => {
          this.$message.error("操作失败");
        });
        this.showDirMoveDialog = false;
      }
    },
    editFolder(node) {
      this.folder.id = node.uuid;
      this.folder.name = node.label;
      this.showEditFolderDialog = true;
    },
    lockFolder(node) {
      let url = node.locked ? '/folder/unlock' : '/folder/lock'
      this.$axios.post(url, this.$qs.stringify({
        uuid: node.uuid
      })).then(res => {
        if (res.data.code == 0) {
          this.$message.success("操作成功");
          let treeNode = this.$refs.MenuTree.getNode(node.uuid);
          if (treeNode != null) {
            this.$set(treeNode.data, "locked", !node.locked);
          }
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "操作失败");
        }
      }).catch(() => {
        this.$message.error("操作失败");
      });
    },
    delFolder(node) {
      this.$confirm("确认删除?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.$axios.post("/folder/delete", this.$qs.stringify({
          uuid: node.uuid,
        })).then((res) => {
          if (res.data.code == 0) {
            this.$message.success("删除成功");
            const deleData = this.$refs.MenuTree.getNode(node.uuid);
            if (deleData) {
              this.$refs.MenuTree.remove(deleData);
            }
            this.$store.commit("setRecyle", true);
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "删除失败");
          }
        }).catch(() => {
          this.$message.error("删除失败");
        });
      });
    },
    submitEditFolder() {
      this.$refs.editFolderForm.validate((valid) => {
        if (valid) {
          this.editLoading = true;
          this.$axios.post("/folder", this.$qs.stringify({
            uuid: this.folder.id,
            dirName: this.folder.name,
          })).then(res => {
            this.editLoading = false;
            if (res.data.code == 0) {
              this.$message.success("修改成功");
              const data = {
                uuid: this.folder.id,
                name: this.folder.name,
              };
              this.refreshAssets(data, false);
              this.showEditFolderDialog = false;
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "修改失败");
            }
          }).catch(() => {
            this.$message.error("修改失败");
            this.editLoading = false;
          });
        }
      });
    },
    handleNodeClick(data) {
      if (!data.leaf) {
        return
      }

      if (data.id === 'pkg') {
        this.activateAssets(data.uuid)
        return
      }
      if (data.id === 'recycle') {
        this.activeRecycle()
        return
      }

      const isTemplate = data.type.indexOf("tpl_") != "-1"
      this.activateAssets(data.uuid, isTemplate);
    },
    activeRecycle() {
      this.activeTab = "recycle";
      let exist;
      this.assetsArr.some((e, index) => {
        if (e.uuid == "recycle") {
          exist = true;
          this.$refs.recycledom[0].initData();
          return true;
        }
      });
      if (!exist) {
        this.assetsArr.push({
          uuid: "recycle",
          type: "recycle",
          id: "recycle",
          name: "回收站",
        });
        this.expandedKey = [];
        this.expandedKey.push("recycle");
        this.$nextTick(() => {
          this.$refs.MenuTree.setCurrentKey("recycle");
        })
      }
    },
    //树菜单拖拽
    allowDrop(draggingNode, dropNode, type) {
      const gingNodeType = draggingNode.data.type;
      const dropNodeLeaf = dropNode.data.leaf;
      const dropNodeIdType =
        dropNode.data.type.indexOf("menu-") != -1
          ? dropNode.data.type.substring(5)
          : dropNode.data.type;
      const dropNodeId = dropNode.data.id;
      //相同资源文件类型，且目标文件不是资源文件
      if (!dropNodeLeaf && gingNodeType == dropNodeIdType) {
        return true;
      } else {
        return false;
      }
    },
    nodeDrop(draggingNode, dropNode) {
      const uuid = draggingNode.data.uuid;
      //判断是否为资源根节点
      const dirParentId =
        dropNode.data.type.indexOf("menu-") != -1 ? 0 : dropNode.data.uuid;

      this.$axios
        .post(
          "/assets/edit",
          this.$qs.stringify({
            uuid,
            dirParentId,
          })
        )
        .then((res) => {
          if (res.data.code == 0) {
            this.$message.success("操作成功");
            this.loadAssetsGroup();
          } else {
            this.loadAssetsGroup();
            this.$message.error(res.data.msg ? res.data.msg : "操作失败");
          }
        })
        .catch(() => {
          this.$message.error("操作失败");
        });
    },
    allowDrag(draggingNode) {
      return draggingNode.data.leaf;
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    handleCommand(type, command) {
      switch (command.command) {
        case "edit":
          this.editFolder(command.data);
          break;
        case "lock":
          if (type == 'DIR') {
            this.lockFolder(command.data);
          } else {
            this.lockAssets(command.data)
          }
          break;
        case "del":
          if (type == 'DIR') {
            this.delFolder(command.data);
          } else {
            this.delAssets(command.data)
          }
          break;
        case "move":
          this.moveNodeFolder(command.data);
          break;
        case "copy":
          this.copyNodeAction(command.data);
          break;
      }
    },
    refreshAssets(assets, isFile = true) {
      const node = this.$refs.MenuTree.getNode(assets.uuid);
      if (node != null) {
        this.assetsArr.forEach((item) => {
          if (item.uuid == assets.uuid) {
            item.name = assets.name;
            item.locked = assets.locked
          }
        });
        this.$set(node.data, "label", assets.name);
        this.$set(node.data, "locked", assets.locked);
        if (!assets.locked) {
          this.$set(node.data, "editor", null);
        }
      }
    },
    //上传
    importFact(e) {
      let form = new FormData();
      form.append("file", e.raw);
      form.append("projectUuid", this.$route.params.uuid);
      this.$axios.post("/assets/importJavaFile", form).then((res) => {
        if (res.data.code == 0) {
          this.$message.success("上传成功");
          const isTemplate = false;
          const isOpen = false;
          res.data.data.forEach((item) => {
            const data = {
              name: item.name,
              uuid: item.uuid,
              dirParentId: "fact",
              type: "fact",
              leaf: true,
            };
            this.appendTree(data, isTemplate, isOpen);
            this.$store.dispatch('loadBoms', {
              projectUuid: this.$route.params.uuid,
              ignoreFunc: true,
              ignoreModel: true,
              ignoreThirdApi: true
            })
          });
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "上传失败");
        }
      });
    },
    searchResources() {
      const searchVal = {
        searchInput: this.filterText,
        searchCondition: this.searchCondition,
      };
      let isSearch = false;
      this.assetsArr.forEach((item, index) => {
        if (item.type == "search") {
          this.assetsArr[index].searchVal = searchVal;
          isSearch = true;
        }
      });
      if (!isSearch) {
        this.assetsArr.push({
          uuid: "search",
          type: "search",
          id: "search",
          name: "搜索",
          searchVal,
        });
      }
      this.activeTab = "search";
    },
    //新增模板
    addTemplate() {
      const data = this.$store.state.addTreeNode;
      const isTemplate = true;
      this.appendTree(data, isTemplate);
      this.$store.commit("setAddTreeNode", {});
    },
    //还原资源文件
    reduction(data) {
      const isTemplate = false;
      const isOpen = false;
      let isLoadBoms = false;
      data.forEach(item => {
        if (item.type == "face" || item.type == "constant") {
          isLoadBoms = true;
        }
        this.appendTree(item, isTemplate, isOpen);
      });
      if (isLoadBoms) {
        this.$store.dispatch("loadBoms", {
          projectUuid: this.$route.params.uuid,
          ignoreFunc: true,
          ignoreModel: true,
          ignoreThidApi: true,
        });
      }
    },
    //判断资源文件是否只读
    isAssetsReadonly(assets) {
      return (
        assets.isTemplate ||
        (assets.editor && assets.editor.uuid != this.$store.getters.userUuid)
      );
    },
    //锁定编辑
    lockEdit(assets) {
      if (!assets.editor) {
        this.doLockEdit(assets.uuid, true);
      } else if (assets.editor.uuid != this.$store.getters.userUuid) {
        const editorName = assets.editor.realname;
        const editTime = this.$moment(assets.editTime).format("MM-DD HH:MM");
        this.$message.warning({
          message: `${editorName}正在编辑，编辑时间：${editTime}`,
          showClose: true,
          duration: 5000,
        });
        const treeNode = this.$refs.MenuTree.getNode(assets.uuid)
        if (treeNode != null && !treeNode.data.editor) {
          this.$set(treeNode.data, 'editor', assets.editor)
        }
      }
    },
    //批量解锁
    unlockEdit(arr) {
      const uuids = [];
      arr.forEach((e) => {
        if (this.canUnlock(e)) {
          uuids.push(e.uuid);
        }
      });
      if (uuids.length > 0) {
        this.doLockEdit(uuids.join(","), false);
      }
    },
    // 调用接口锁定，解锁编辑
    doLockEdit(uuids, lock) {
      this.$axios.post("/assets/lockedit", this.$qs.stringify({
        uuids: uuids,
        lock
      })).then(res => {
        if (res.data.code == 0 && this.$refs.MenuTree) {
          uuids.split(',').forEach(uuid => {
            const node = this.$refs.MenuTree.getNode(uuid)
            if (node != null) {
              if (lock) {
                this.$set(node.data, 'editor', {uuid: this.$store.getters.userUuid})
              } else {
                this.$set(node.data, 'editor', null)
              }
            }
          })
        }
      });
    },
    canUnlock(assets) {
      const arr = [
        "fact",
        "constant",
        "guidedrule",
        "rulescript",
        "ruletable",
        "ruletree",
        "scorecard",
        "ruleflow",
        "testcase",
      ];
      if (arr.indexOf(assets.type) == -1) {
        return false;
      }
      if (assets.isTemplate) {
        return false;
      }
      return !this.isAssetsReadonly(assets);
    },
    importAssets(e) {
      let config = {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      };
      let form = new FormData();
      form.append("file", e.raw);
      form.append("projectUuid", this.$route.params.uuid);
      this.$axios.post("/assets/import", form, config).then(res => {
        if (res.data.code == 0) {
          this.$message.success("导入成功");
          const isTemplate = false;
          const isOpen = false;
          res.data.data.tree.forEach((item) => {
            this.appendTree(item, isTemplate, isOpen);
          });
          this.$store.dispatch("loadBoms", {
            projectUuid: this.$route.params.uuid,
            ignoreFunc: true,
            ignoreModel: true,
            ignoreThidApi: true,
            callback: () => {
              this.activateAssets(res.data.data.uuid);
            },
          });
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "导入失败");
        }
        this.importLoading = false
      }).catch(() => {
        this.importLoading = false
      })
    },
    lockAssets(node) {
      const url = node.locked || node.editor ? '/assets/unlock' : '/assets/lock'
      this.$axios.post(url, this.$qs.stringify({
        uuid: node.uuid
      })).then(res => {
        if (res.data.code == 0) {
          this.$message.success('操作成功')
          const treeNode = this.$refs.MenuTree.getNode(node.uuid)
          if (treeNode != null) {
            if (node.editor) {
              this.$set(treeNode.data, 'locked', false)
              this.$set(treeNode.data, 'editor', null)
              this.assetsArr.forEach(item => {
                if (item.uuid == node.uuid) {
                  item.locked = false
                  item.editor = null
                }
              })
            } else {
              this.$set(treeNode.data, 'locked', !node.locked)
              this.assetsArr.forEach(item => {
                if (item.uuid == node.uuid) {
                  item.locked = node.locked
                }
              })
            }
          }
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '操作失败')
        }
      }).catch(() => {
        this.$message.error('操作失败')
      })
    },
    /**
     * 左侧菜单删除资源文件
     */
    delAssets(node) {
      this.$confirm('确认删除？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let url
        let params = {}
        if (node.type == 'testcase') {
          url = '/testcase/delete'
          params.caseUuid = node.uuid
        } else {
          params.uuid = node.uuid
          if (node.type.startsWith('tpl_')) {
            url = '/template/delete'
          } else {
            url = '/assets/delete'
          }
        }
        this.$axios.post(url, this.$qs.stringify(params)).then(res => {
          if (res.data.code == 0) {
            this.$message.success('删除成功')
            this.removeTab(node.uuid)
            const deleData = this.$refs.MenuTree.getNode(node.uuid)
            if (deleData) {
              this.$refs.MenuTree.remove(deleData)
            }
            this.$store.commit('setRecycle', true)
            if (node.type == 'fact' || node.type == 'constant') {
              this.$store.dispatch('loadBoms', {
                projectUuid: this.$route.params.uuid,
                ignoreFunc: true,
                ignoreModel: true,
                ignoreThirdApi: true
              })
            }
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '删除失败')
          }
        }).catch(() => {
          this.$message.error('删除失败')
        })
      })
    },
    unlockEditOnunload() {
      this.unlockEdit(this.assetsArr);
    }
  },
  beforeRouteLeave(to, from, next){
    this.unlockEdit(this.assetsArr);
    this.unbindEvent();
    next();
  },
};
</script>

<style scoped>
  .aside-wrap {
    background-color: #545c64;
  }
  .tree-wrap .el-tree {
    background-color: #545c64;
    color: #fff;
    margin-bottom: 12px;
    min-width: 100%;
    display: inline-block !important;
  }
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }
  .header-wrap {
    display: flex;
    justify-content: space-between;
  }
</style>
