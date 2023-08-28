<template>
  <el-dialog
    ref="dialog"
    :visible="show"
    @update:visible="close"
    @opened="init"
    @closed="closed"
    v-loading="loading"
    element-loading-text="数据加载中..."
    width="80%"
    top="10px"
    append-to-body>
    <template slot="title">
      版本比较
      <el-tooltip placement="right" v-if="assetsType == 'ruleflow'">
        <div slot="content">
          1、右侧新增的节点以绿色方框高亮展示
          <br>
          2、左侧删除的节点以灰色方框高亮展示
          <br>
          3、两侧更新的节点以红色方框高亮展示
        </div>
        <i class="el-icon-question"></i>
      </el-tooltip>
    </template>
    <template v-if="baseVersion != null && compareVersion != null">
      <div class="diffContent" v-if="assetsType == 'ruleflow'">
        <div class="diffComponent">
          <span> 版本V{{assetsVersion[1].versionNo}}</span>
          <div
            id="compareFlowDiagram"
            style="flex-grow: 1; height: 500px; border: solid 1px #ddd;">
          </div>
        </div>
        <div class="divider"></div>
        <div class="diffComponent">
          <span> 版本V{{assetsVersion[0].versionNo}}</span>
          <div
            id="baseFlowDiagram"
            style="flex-grow: 1; height: 500px; border: solid 1px #ddd;">
          </div>
        </div>
      </div>
      <div ref="diffContainer" style="width: 100%; height: 500px;"></div>
    </template>

    <template v-if="assetsType == 'ruleflow'">
      <ul class="el-dropdown-menu el-popper" ref="contextMenu"
          style="top: -1000px; transform-origin: center bottom 0px;">
        <li class="el-dropdown-menu__item" ref="contextMenu-setRule" @click="showRule">
          <i class="iconfont iconguize"></i>
          查看规则
        </li>
        <li class="el-dropdown-menu__item" ref="contextMenu-setDecision" @click="showDecision">
          <i class="iconfont iconfenzhijiedian"></i>
          查看决策
        </li>
        <li class="el-dropdown-menu__item" ref="contextMenu-setScript" @click="showScript">
          <i class="iconfont iconjiaobenrenwu"></i>
          查看脚本
        </li>
      </ul>

      <el-dialog title="规则文件列表" :top="$dialogTop" width="60%" :visible.sync="showRuleDialog" append-to-body>
        <el-table :data="rules">
          <el-table-column prop="name" label="文件名称"></el-table-column>
          <el-table-column prop="versionNo" label="版本号">
            <template slot-scope="scope">
              {{scope.row.versionNo||(scope.row.versionNo==0)?'V'+scope.row.versionNo:""}}
            </template>
          </el-table-column>
          <el-table-column label="版本描述" prop="versionDes" show-overflow-tooltip></el-table-column>
          <el-table-column label="创建时间">
            <template v-if="scope.row.createTime" slot-scope="scope">
              {{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}
            </template>
          </el-table-column>
          <el-table-column prop="type" label="文件类型"
                           :formatter="row => {return this.$utils.getAssetsTypeText(row.type)}"></el-table-column>
          <el-table-column label="操作" width="60">
            <template slot-scope="scope">
              <el-button @click.stop="viewAssets(scope.row)" type="text">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div slot="footer">
          <el-button @click="showRuleDialog = false" icon="el-icon-error">取 消</el-button>
        </div>
      </el-dialog>

      <el-dialog title="查看脚本" :visible.sync="showScriptDialog" append-to-body>
        <rhs v-model="script.rhs" from-flow readonly></rhs>
        <div slot="footer">
          <el-button @click="showScriptDialog = false" icon="el-icon-error">取 消</el-button>
        </div>
      </el-dialog>

      <el-dialog title="查看决策" :visible.sync="showDecisionDialog" append-to-body>
        <el-form :model="decisionForm" label-width="80px" inline>
          <el-form-item label="分支类型">
            <el-select v-model="decisionForm.type" placeholder="请选择" disabled>
              <el-option label="条件" value="CONDITION"></el-option>
              <el-option label="百分比" value="PERCENTAGE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="策略配置" v-if="decisionForm.type == 'CONDITION'">
            <el-select v-model="decisionForm.strategy" placeholder="请选择" disabled>
              <el-option label="XOR" value="XOR"></el-option>
              <el-option label="OR" value="OR"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <el-table :data="decisionForm.connections">
          <el-table-column :label="decisionForm.type == 'CONDITION' ? '条件' : '百分比(单位%)'">
            <template slot-scope="scope">
              <template v-if="decisionForm.type == 'CONDITION'">
                <span v-html="$lhs.getText(scope.row.lhs, true)" style="white-space: normal; line-height: 1.5;"></span>
              </template>
              <template v-else>
                <el-input-number
                  v-model="scope.row.percentage"
                  :controls="false"
                  disabled
                  :min="0"
                  :max="maxPercentage(scope.$index)">
                </el-input-number>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="流向">
            <template slot-scope="scope">
              <el-select v-model="scope.row.key" placeholder="请选择" disabled>
                <el-option v-for="node in decisionForm.toNodes" :key="node.key" :label="node.name"
                           :value="node.key"></el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
        <div slot="footer">
          <el-button @click="showDecisionDialog" icon="el-icon-error">取 消</el-button>
        </div>
      </el-dialog>

      <assets-version
        :assets-uuid="viewingAssets.uuid"
        :assets-version="viewingAssets.version"
        :show.sync="viewingAssets.show">
      </assets-version>
    </template>
  </el-dialog>
</template>

<script>
  import rhs from '../components/rhs'

  const $ = go.GraphObject.make
  const _changedAdornmentTpl = $(go.Adornment, 'Auto',
    $(go.Shape, {fill: null, stroke: 'red', strokeWidth: 3}),
    $(go.Placeholder, {margin: 0})
  )
  const _addedAdornmentTpl = $(go.Adornment, 'Auto',
    $(go.Shape, {fill: null, stroke: 'green', strokeWidth: 3}),
    $(go.Placeholder, {margin: 0})
  )
  const _removedAdornmentTpl = $(go.Adornment, 'Auto',
    $(go.Shape, {fill: null, stroke: 'grey', strokeWidth: 3}),
    $(go.Placeholder, {margin: 0})
  )

  export default {
    name: "assets-diff",
    props: {
      assetsUuid: String,
      assetsVersion: Array,
      assetsType: String,
      show: Boolean
    },
    components: {
      rhs
    },
    data() {
      return {
        loading: false,
        baseVersion: null,
        compareVersion: null,
        baseFlowDiagram: null,
        compareFlowDiagram: null,
        activeNode: null,
        showRuleDialog: false,
        rules: [],
        showScriptDialog: false,
        script: {},
        decisionForm: {},
        showDecisionDialog: false,
        baseRuleContent: null,
        compareRuleContent: null,
        viewingAssets: {
          uuid: '',
          version: 0,
          show: false
        },
        diffEditor: null,
        originalModel: null,
        modifiedModel: null
      }
    },
    methods: {
      init() {
        this.loading = true
        this.assetsVersion.sort((a, b) => b.versionNo - a.versionNo)
        this.$axios.get('/assets/compare', {
          params: {
            assetUuid: this.assetsUuid,
            assetType: this.assetsType,
            baseVersion: this.assetsVersion[0].versionNo,
            compareVersion: this.assetsVersion[1].versionNo
          }
        }).then(res => {
          if (res.data.code == 0) {
            this.baseVersion = res.data.data.baseVersion
            this.compareVersion = res.data.data.compareVersion
            if (this.assetsType == 'ruleflow') {
              this.loading = false;
              this.$nextTick(() => {
                this.renderFlowDiagram()
              })
            } else {
              this.renderRuleContent()
            }
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '查询失败');
            this.loading = false;
          }
        }).catch(e => {
          console.error(e)
          this.$message.error('查询失败')
          this.loading = false;
        })
      },
      renderFlowDiagram() {
        this.renderBaseFlowDiagram()
        this.renderCompareFlowDiagram()
        this.renderDiff()
      },
      renderBaseFlowDiagram() {
        const self = this
        const baseFlow = JSON.parse(this.baseVersion.content)
        const diagram = $(go.Diagram, 'baseFlowDiagram',
          {
            initialContentAlignment: go.Spot.Center,
            allowDrop: true,
            scrollsPageOnFocus: false,
            isReadOnly: true,
            "grid.gridCellSize": new go.Size(30, 20),
            "draggingTool.isGridSnapEnabled": true,
            "resizingTool.isGridSnapEnabled": true,
            "rotatingTool.snapAngleMultiple": 90,
            "rotatingTool.snapAngleEpsilon": 45
          }
        )

        // This is the actual HTML context menu:
        var cxElement = this.$refs.contextMenu

        // Since we have only one main element, we don't have to declare a hide method,
        // we can set mainElement and GoJS will hide it automatically
        var myContextMenu = $(go.HTMLInfo, {
          show: showContextMenu,
          hide: this.hideContextMenu,
          mainElement: cxElement
        });

        // We don't want the div acting as a context menu to have a (browser) context menu!
        cxElement.addEventListener("contextmenu", function (e) {
          e.preventDefault();
          return false;
        }, false);

        function showContextMenu(obj, diagram, tool) {
          const category = obj.part.data.category
          if (self.readonly && (category == 'Start' || category == 'Diverge' || category == 'Converge')) return

          self.activeNode = obj.part
          self.$refs['contextMenu-setRule'].style.display = category == 'Rule' ? 'block' : 'none'
          self.$refs['contextMenu-setDecision'].style.display = category == 'Decision' ? 'block' : 'none'
          self.$refs['contextMenu-setScript'].style.display = category == 'Script' ? 'block' : 'none'
          // we don't bother overriding positionContextMenu, we just do it here:
          const mousePt = diagram.lastInput.viewPoint;
          let x = mousePt.x, y = mousePt.y
          if (x + cxElement.offsetWidth > diagram.div.offsetWidth) {
            x -= cxElement.offsetWidth
          }
          if (y + cxElement.offsetHeight > diagram.div.offsetHeight) {
            y -= cxElement.offsetHeight
          }
          const dialogBody = self.$refs.dialog.$el.querySelector('.el-dialog__body')
          cxElement.style.left = (x + 20 + dialogBody.offsetWidth / 2) + "px";
          cxElement.style.top = (y + 100) + "px";
        }

        diagram.nodeTemplateMap.add('Start',
          $(go.Node, 'Table', self.nodeStyle(), {},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Circle',
                {
                  fill: '#79C900',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: false,
                  toMaxLinks: 0
                }
              ),
              $(go.TextBlock, '开始', self.textStyle(), {}, new go.Binding('text', 'name').makeTwoWay()),
            )
          )
        )

        diagram.nodeTemplateMap.add('Rule',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
            $(go.Panel, 'Auto',
              $(go.Shape, 'RoundedRectangle',
                {
                  fill: '#faaa00',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '规则', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )


        diagram.nodeTemplateMap.add('Decision',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Diamond',
                {
                  fill: '#56abe4',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: Infinity,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '决策', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Diverge',
          $(go.Node, 'Table', self.nodeStyle(), {},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Diamond',
                {
                  fill: '#eb4f38',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: Infinity,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '分支', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Converge',
          $(go.Node, 'Table', self.nodeStyle(), {},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Diamond',
                {
                  fill: '#ae58a7',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: true,
                  toMaxLinks: Infinity
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '聚合', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Script',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
            $(go.Panel, 'Auto',
              $(go.Shape, 'RoundedRectangle',
                {
                  fill: '#669999',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '脚本', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.linkTemplate = $(go.Link,
          {
            routing: go.Link.AvoidsNodes,
            curve: go.Link.JumpOver,
            corner: 5, toShortLength: 4,
            relinkableFrom: true,
            relinkableTo: true,
            resegmentable: true,
            mouseEnter: function (e, link) {
              link.findObject("HIGHLIGHT").stroke = "rgba(30,144,255,0.2)";
            },
            mouseLeave: function (e, link) {
              link.findObject("HIGHLIGHT").stroke = "transparent";
            },
            selectionAdorned: false
          },
          new go.Binding("points").makeTwoWay(),
          $(go.Shape,  // the highlight shape, normally transparent
            {isPanelMain: true, strokeWidth: 8, stroke: "transparent", name: "HIGHLIGHT"}),
          $(go.Shape,  // the link path shape
            {isPanelMain: true, stroke: "gray", strokeWidth: 2},
            new go.Binding("stroke", "isSelected", function (sel) {
              return sel ? "dodgerblue" : "gray";
            }).ofObject()),
          $(go.Shape,  // the arrowhead
            {toArrow: "standard", strokeWidth: 0, fill: "gray"}),
          $(go.Panel, "Auto",  // the link label, normally not visible
            {name: "LABEL", segmentIndex: 2, segmentFraction: 0.5},
            new go.Binding("visible", "visible").makeTwoWay(),
            $(go.Shape, "RoundedRectangle",  // the label shape
              {visible: false, fill: "#F8F8F8", strokeWidth: 0}),
            $(go.TextBlock,  // the label
              {
                textAlign: "center",
                font: "10pt helvetica, arial, sans-serif",
                stroke: "#333333",
                editable: true,
                textEdited: (ref, preText, curText) => {
                  const el = ref.panel.findMainElement()
                  curText = curText.trim()
                  if (curText) {
                    ref.text = curText.trim()
                    el.visible = true
                  } else {
                    el.visible = false
                  }
                }
              },
              new go.Binding("text").makeTwoWay())
          )
        )

        diagram.toolManager.linkingTool.temporaryLink.routing = go.Link.Orthogonal;
        diagram.toolManager.relinkingTool.temporaryLink.routing = go.Link.Orthogonal;
        const model = go.Model.fromJson(baseFlow)
        diagram.model = $(go.GraphLinksModel, {
          linkFromPortIdProperty: 'fromPort',
          linkToPortIdProperty: 'toPort',
          nodeDataArray: model.nodeDataArray,
          linkDataArray: model.linkDataArray
        })

        this.baseFlowDiagram = diagram
      },
      renderCompareFlowDiagram() {
        const self = this
        const compareFlow = JSON.parse(this.compareVersion.content)
        const diagram = $(go.Diagram, 'compareFlowDiagram',
          {
            initialContentAlignment: go.Spot.Center,
            allowDrop: true,
            scrollsPageOnFocus: false,
            isReadOnly: true,
            "grid.gridCellSize": new go.Size(30, 20),
            "draggingTool.isGridSnapEnabled": true,
            "resizingTool.isGridSnapEnabled": true,
            "rotatingTool.snapAngleMultiple": 90,
            "rotatingTool.snapAngleEpsilon": 45
          }
        )

        // This is the actual HTML context menu:
        var cxElement = this.$refs.contextMenu

        // Since we have only one main element, we don't have to declare a hide method,
        // we can set mainElement and GoJS will hide it automatically
        var myContextMenu = $(go.HTMLInfo, {
          show: showContextMenu,
          hide: this.hideContextMenu,
          mainElement: cxElement
        });

        // We don't want the div acting as a context menu to have a (browser) context menu!
        cxElement.addEventListener("contextmenu", function (e) {
          e.preventDefault();
          return false;
        }, false);

        function showContextMenu(obj, diagram, tool) {
          const category = obj.part.data.category
          if (self.readonly && (category == 'Start' || category == 'Diverge' || category == 'Converge')) return

          self.activeNode = obj.part
          self.$refs['contextMenu-setRule'].style.display = category == 'Rule' ? 'block' : 'none'
          self.$refs['contextMenu-setDecision'].style.display = category == 'Decision' ? 'block' : 'none'
          self.$refs['contextMenu-setScript'].style.display = category == 'Script' ? 'block' : 'none'
          // we don't bother overriding positionContextMenu, we just do it here:
          const mousePt = diagram.lastInput.viewPoint;
          let x = mousePt.x, y = mousePt.y
          if (y + cxElement.offsetHeight > diagram.div.offsetHeight) {
            y -= cxElement.offsetHeight
          }
          const dialogBody = self.$refs.dialog.$el.querySelector('.el-dialog__body')
          cxElement.style.left = (x + 20) + "px";
          cxElement.style.top = (y + 100) + "px";
        }

        diagram.nodeTemplateMap.add('Start',
          $(go.Node, 'Table', self.nodeStyle(), {},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Circle',
                {
                  fill: '#79C900',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: false,
                  toMaxLinks: 0
                }
              ),
              $(go.TextBlock, '开始', self.textStyle(), {}, new go.Binding('text', 'name').makeTwoWay()),
            )
          )
        )

        diagram.nodeTemplateMap.add('Rule',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
            $(go.Panel, 'Auto',
              $(go.Shape, 'RoundedRectangle',
                {
                  fill: '#faaa00',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '规则', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )


        diagram.nodeTemplateMap.add('Decision',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Diamond',
                {
                  fill: '#56abe4',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: Infinity,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '决策', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Diverge',
          $(go.Node, 'Table', self.nodeStyle(), {},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Diamond',
                {
                  fill: '#eb4f38',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: Infinity,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '分支', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Converge',
          $(go.Node, 'Table', self.nodeStyle(), {},
            $(go.Panel, 'Auto',
              $(go.Shape, 'Diamond',
                {
                  fill: '#ae58a7',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: true,
                  toMaxLinks: Infinity
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '聚合', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Script',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
            $(go.Panel, 'Auto',
              $(go.Shape, 'RoundedRectangle',
                {
                  fill: '#669999',
                  stroke: null,
                  portId: "",
                  cursor: "pointer",
                  fromLinkable: true,
                  fromMaxLinks: 1,
                  toLinkable: true,
                  toMaxLinks: 1
                },
                new go.Binding("figure", "figure")
              ),
              $(go.TextBlock, '脚本', self.textStyle(),
                {
                  margin: 8,
                  maxSize: new go.Size(160, NaN),
                  wrap: go.TextBlock.WrapFit
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.linkTemplate = $(go.Link,
          {
            routing: go.Link.AvoidsNodes,
            curve: go.Link.JumpOver,
            corner: 5, toShortLength: 4,
            relinkableFrom: true,
            relinkableTo: true,
            resegmentable: true,
            mouseEnter: function (e, link) {
              link.findObject("HIGHLIGHT").stroke = "rgba(30,144,255,0.2)";
            },
            mouseLeave: function (e, link) {
              link.findObject("HIGHLIGHT").stroke = "transparent";
            },
            selectionAdorned: false
          },
          new go.Binding("points").makeTwoWay(),
          $(go.Shape,  // the highlight shape, normally transparent
            {isPanelMain: true, strokeWidth: 8, stroke: "transparent", name: "HIGHLIGHT"}),
          $(go.Shape,  // the link path shape
            {isPanelMain: true, stroke: "gray", strokeWidth: 2},
            new go.Binding("stroke", "isSelected", function (sel) {
              return sel ? "dodgerblue" : "gray";
            }).ofObject()),
          $(go.Shape,  // the arrowhead
            {toArrow: "standard", strokeWidth: 0, fill: "gray"}),
          $(go.Panel, "Auto",  // the link label, normally not visible
            {name: "LABEL", segmentIndex: 2, segmentFraction: 0.5},
            new go.Binding("visible", "visible").makeTwoWay(),
            $(go.Shape, "RoundedRectangle",  // the label shape
              {visible: false, fill: "#F8F8F8", strokeWidth: 0}),
            $(go.TextBlock,  // the label
              {
                textAlign: "center",
                font: "10pt helvetica, arial, sans-serif",
                stroke: "#333333",
                editable: true,
                textEdited: (ref, preText, curText) => {
                  const el = ref.panel.findMainElement()
                  curText = curText.trim()
                  if (curText) {
                    ref.text = curText.trim()
                    el.visible = true
                  } else {
                    el.visible = false
                  }
                }
              },
              new go.Binding("text").makeTwoWay())
          )
        )

        diagram.toolManager.linkingTool.temporaryLink.routing = go.Link.Orthogonal;
        diagram.toolManager.relinkingTool.temporaryLink.routing = go.Link.Orthogonal;
        const model = go.Model.fromJson(compareFlow)
        diagram.model = $(go.GraphLinksModel, {
          linkFromPortIdProperty: 'fromPort',
          linkToPortIdProperty: 'toPort',
          nodeDataArray: model.nodeDataArray,
          linkDataArray: model.linkDataArray
        })

        this.compareFlowDiagram = diagram
      },
      hideContextMenu() {
        this.$refs.contextMenu.style.top = '-1000px'
      },
      renderDiff() {
        const d1 = this.baseFlowDiagram
        const d2 = this.compareFlowDiagram
        d1.model.nodeDataArray.forEach(e => {
          const node1 = d1.findNodeForKey(e.key)
          const node2 = d2.findNodeForKey(e.key)
          if (node2 == null) {
            // 新增节点
            this.addAdornment(node1, _addedAdornmentTpl)
          } else if (this.isDiff(node1, node2)) {
            // 变更节点
            this.addAdornment(node1, _changedAdornmentTpl)
          }
        })

        d2.model.nodeDataArray.forEach(e => {
          const node1 = d1.findNodeForKey(e.key)
          const node2 = d2.findNodeForKey(e.key)
          if (node1 == null) {
            // 删除节点
            this.addAdornment(node2, _removedAdornmentTpl)
          } else if (this.isDiff(node1, node2)) {
            // 变更节点
            this.addAdornment(node2, _changedAdornmentTpl)
          }
        })
      },
      isDiff(node1, node2) {
        const category = node1.data.category
        if (category == 'Rule') {
          if (!this.$utils.equals(node1.data.rules, node2.data.rules)) {
            return true
          }
        } else if (category == 'Script') {
          if (!this.$utils.equals(node1.data.script, node2.data.script)) {
            return true
          }
        } else if (category == 'Decision') {
          if (!this.$utils.equals(node1.data.decision, node2.data.decision)) {
            return true
          }
        }
        return false
      },
      addAdornment(part, adTpl) {
        const ad = adTpl.copy()
        ad.adornedObject = part
        ad.location = part.position
        part.addAdornment('diff', ad)
      },
      textStyle() {
        return {
          font: "bold 11pt Helvetica, Arial, sans-serif",
          stroke: "whitesmoke"
        }
      },
      nodeStyle() {
        return [
          // The Node.location comes from the "loc" property of the node data,
          // converted by the Point.parse static method.
          // If the Node.location is changed, it updates the "loc" property of the node data,
          // converting back using the Point.stringify static method.
          new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
          {
            // the Node.location is at the center of each node
            locationSpot: go.Spot.Center
          }
        ]
      },
      close() {
        this.$emit('update:show', false)
      },
      closed() {
        this.baseVersion = null;
        this.compareVersion = null;
        this.diffEditor.dispose();
        this.originalModel.dispose();
        this.modifiedModel.dispose();
      },
      showRule() {
        if (this.activeNode.data.rules) {
          this.rules = this.activeNode.data.rules
        } else {
          this.rules = []
        }
        this.showRuleDialog = true
        this.hideContextMenu()
      },
      showDecision() {
        if (!this.activeNode.data.decision) this.activeNode.data.decision = {}
        this.$set(this.decisionForm, 'type', this.activeNode.data.decision.type || 'CONDITION')
        this.$set(this.decisionForm, 'strategy', this.activeNode.data.decision.strategy || 'XOR')

        let connections = [], toNodes = []
        for (let it = this.activeNode.findLinksOutOf().iterator; it.next();) {
          const data = it.value.toNode.data
          let lhs, percentage
          if (this.activeNode.data.decision.connections) {
            this.activeNode.data.decision.connections.some(e => {
              if (e.key == data.key) {
                if (this.activeNode.data.decision.type == 'CONDITION') {
                  lhs = this.$utils.copy(e.lhs)
                } else {
                  percentage = e.percentage
                }
                return true
              }
            })
          }
          connections.push({
            key: data.key,
            lhs: lhs,
            percentage: percentage
          })
          toNodes.push({
            key: data.key,
            name: data.name
          })
        }
        this.decisionForm.connections = connections
        this.decisionForm.toNodes = toNodes
        this.showDecisionDialog = true
        this.hideContextMenu()
      },
      showScript() {
        if (this.activeNode.data.script) {
          this.script = this.activeNode.data().script
        } else {
          this.script = {}
        }
        this.showScriptDialog = true
        this.hideContextMenu()
      },
      viewAssets(row) {
        this.viewingAssets.uuid = row.uuid
        this.viewingAssets.version = row.versionNo
        this.viewingAssets.show = true
      },
      renderRuleContent() {
        const baseJson = this.baseVersion.content ? JSON.parse(this.baseVersion.content) : {}
        const compareJson = this.compareVersion.content ? JSON.parse(this.compareVersion.content) : {}
        if (this.assetsType == 'guidedrule') {
          this.baseRuleContent = this.getGuidedRuleText(baseJson)
          this.compareRuleContent = this.getGuidedRuleText(compareJson)
        } else if (this.assetsType == 'rulescript') {
          this.baseRuleContent = this.getRuleScriptText(baseJson)
          this.compareRuleContent = this.getRuleScriptText(compareJson)
        } else if (this.assetsType == 'ruletable') {
          this.baseRuleContent = this.getRuleTableText(baseJson)
          this.compareRuleContent = this.getRuleTableText(compareJson)
        } else if (this.assetsType == 'ruletree') {
          this.baseRuleContent = this.getRuleTreeText(baseJson)
          this.compareRuleContent = this.getRuleTreeText(compareJson)
        } else if (this.assetsType == 'scorecard') {
          this.baseRuleContent = this.getScorecardText(baseJson)
          this.compareRuleContent = this.getScorecardText(compareJson)
        }

        this.baseRuleContent = `版本V${this.assetsVersion[0].versionNo}\n\n` + this.baseRuleContent
        this.compareRuleContent = `版本V${this.assetsVersion[1].versionNo}\n\n` + this.compareRuleContent

        this.initEditor();
      },
      async initEditor() {
        try {
          const monaco = await import('monaco-editor/esm/vs/editor/editor.api');
          const originalModel = monaco.editor.createModel(this.compareRuleContent, 'text/plain')
          const modifiedModel = monaco.editor.createModel(this.baseRuleContent, 'text/plain')
          this.$nextTick(() => {
            const diffEditor = monaco.editor.createDiffEditor(
              this.$refs.diffContainer,
              {
                readOnly: true
              }
            )
            diffEditor.setModel({
              original: originalModel,
              modified: modifiedModel
            })
            this.diffEditor = diffEditor;
            this.originalModel = originalModel;
            this.modifiedModel = modifiedModel;
            this.loading = false;
          });
        } catch(e) {
          this.loading = false;
          this.$message.error('初始化编辑框失败');
        }
      },
      getGuidedRuleText(json) {
        const textArr = []
        json.rules.forEach(rule => {
          const ruleArr = []
          ruleArr.push(`规则 "${rule.name}"`)
          const attrsText = this.getAttrsText(rule.attrs)
          if (attrsText) {
            ruleArr.push(attrsText)
          }
          if (rule.loop) {
            const loopText = this.$store.getters.getFactText(rule.loopTarget.id, rule.loopTarget.fieldId)
            if (loopText) {
              ruleArr.push('循环对象: ' + loopText)
            }
          }
          ruleArr.push('如果')
          const lhsText = this.$lhs.getText(rule.lhs, true, false, rule.loopTarget)
          ruleArr.push(lhsText)
          ruleArr.push('那么')
          ruleArr.push(this.getRhsText(rule.rhs))
          if (rule.other && rule.other.actions && rule.other.actions.length > 0) {
            ruleArr.push('否则')
            ruleArr.push(this.getRhsText(rule.other))
          }
          textArr.push(ruleArr.join('\n') + '\n')
        })
        return textArr.join('\n')
      },
      getRuleScriptText(json) {
        return json.script
      },
      getRuleTableText(json) {
        const textArr = []
        json.rows.forEach((row, i) => {
          const ruleArr = [], attrArr = [], rhsArr = []
          const constraint = {conjunction: 'AND', constraints: []}
          row.forEach((cell, j) => {
            const header = json.headers[j]
            if (header.type == 'CONDITION') {
              cell.constraint.constraints.forEach(c => {
                c.left = {type: 'FACT', fact: header.fact}
              })
              constraint.constraints.push(cell.constraint)
            } else if (header.type == 'ASSIGN') {
              const factText = this.$store.getters.getFactText(header.fact.id, header.fact.fieldId)
              const rhs = factText + ' = ' + this.$lhs.getExpressionText(cell.right)
              rhsArr.push(rhs)
            } else if (header.type == 'ATTR') {
              attrArr.push({name: header.name, value: cell.value})
            }
          })
          const lhs = this.$lhs.getText({constraint}, true, false)
          if (attrArr.length > 0) {
            ruleArr.push(this.getAttrsText(attrArr))
          }
          ruleArr.push('如果')
          ruleArr.push(lhs)
          ruleArr.push('那么')
          ruleArr.push(rhsArr.join('\n'))
          textArr.push(ruleArr.join('\n') + '\n')
        })
        return textArr.join('\n')
      },
      getRuleTreeText(json) {
        const textArr = []
        const ruleArr = []
        const stack = []
        const root = json.tree
        let ruleId = 0

        const pushStack = node => {
          if (stack.length == 0) {
            stack.push(node)
            return
          }

          while (stack[stack.length - 1] != node.parent) {
            stack.pop()
          }
          stack.push(node)
        }

        const getFactNodeOfCondition = conditionNode => {
          const parent = conditionNode.parent
          if (!parent.nodeType || parent.nodeType == 'FACT') {
            if (!parent.constraintMap) {
              parent.constraintMap = {}
            }
            return parent
          }
          return getFactNodeOfCondition(parent)
        }

        const getElseConstraint = conditionNode => {
          const parent = getFactNodeOfCondition(conditionNode)
          let elseConstraint
          parent.children.some(child => {
            if (child.elseConstraint) {
              elseConstraint = child.elseConstraint
              return true
            }
          })
          return elseConstraint
        }

        const removeDuplicateConstraintMap = map => {
          const list = []
          Object.keys(map).forEach(key => {
            let exists = false
            list.some(list2 => {
              if (equalsList(map[key], list2)) {
                exists = true
                return true
              }
            })
            if (!exists) {
              list.push(map[key])
            }
          })
        }

        const equalsList = (list1, list2) => {
          if (list1.length != list2.length) {
            return false
          }

          for (let i = 0; i < list1.length; i++) {
            if (!equals(list1[i], list2[i])) {
              return false
            }
          }
          return true
        }

        const equals = (c1, c2) => {
          try {
            return c1.left.fact == c2.left.fact && c1.op == c2.op && c1.right == c2.right
          } catch(e) {
            return false
          }
        }

        const handleStack = () => {
          ruleId++
          const constraint = {conjunction: 'AND', constraints: []}
          let rhs, left
          stack.forEach(node => {
            const nodeType = node.nodeType
            if (!nodeType || nodeType == 'FACT') {
              left = {type: 'FACT', fact: node.fact}
            } else if (nodeType == 'CONDITION' && node.condition) {
              const condition = node.condition
              if (!condition.other) {
                const tmpConstraint = {
                  left: left,
                  op: condition.op,
                  right: condition.right
                }
                constraint.constraints.push(tmpConstraint)
                const factNode = getFactNodeOfCondition(node)
                let constraints = factNode.constraintMap[ruleId]
                if (!constraints) {
                  constraints = []
                  factNode.constraintMap[ruleId] = constraints
                }
                const elseConstraint = getElseConstraint(node)
                if (elseConstraint) {
                  let siblingConstraint
                  if (constraints.length == 0) {
                    siblingConstraint = {conjunction: 'AND', constraints: []}
                    elseConstraint.constraints.push(siblingConstraint)
                  } else {
                    siblingConstraint = elseConstraint.constraints[elseConstraint.constraints.length - 1]
                  }
                  siblingConstraint.constraints.push(tmpConstraint)

                  const elseChildren = elseConstraint.constraints
                  if (elseChildren.length > 1) {
                    if (equalsList(
                          elseChildren[elseChildren.length - 2].constraints,
                          elseChildren[elseChildren.length - 1].constraints)
                    ) {
                      elseChildren.pop()
                    }
                  }
                }
                constraints.push(tmpConstraint)
              } else {
                const elseConstraint = {conjunction: 'NOT', constraints: []}
                removeDuplicateConstraintMap(node.parent.constraintMap).forEach(list => {
                  const siblingConstraint = {conjunction: 'AND', constraints: []}
                  list.forEach(c => {
                    siblingConstraint.constraints.push(c)
                  })
                  elseConstraint.constraints.push(siblingConstraint)
                })
                constraint.constraints.push(elseConstraint)
                node.elseConstraint = elseConstraint
              }
            } else if (nodeType == 'ACTION') {
              rhs = node.rhs
            }
          })

          ruleArr.push({lhs: {constraint}, rhs})
        }

        const traversal = parent => {
          pushStack(parent)

          if (!parent.children || parent.children.length == 0) {
            handleStack()
            return
          }

          parent.children.forEach(child => {
            child.parent = parent
            traversal(child)
          })
        }

        traversal(root)

        textArr.push(this.getAttrsText(json.attrs))
        ruleArr.forEach(rule => {
          const arr = []
          const lhs = this.$lhs.getText(rule.lhs, false, false)
          arr.push('如果')
          arr.push(lhs)
          arr.push('那么')
          arr.push(this.getRhsText(rule.rhs) + '\n')
          textArr.push(arr.join('\n'))
        })
        return textArr.join('\n')
      },
      getScorecardText(json) {
        const textArr = []
        const attrsText = this.getAttrsText(json.attrs)
        if (attrsText) {
          textArr.push(attrsText)
        }
        textArr.push(this.getScorecardHeaderText(json))
        json.rows.forEach(row => {
          const ruleArr = []
          ruleArr.push('如果')
          let lhs = ''
          if (row.fact && row.fact.id) {
            const factText = this.$store.getters.getFactText(row.fact.id, row.fact.fieldId)
            lhs += factText + ' '
          } else {
            lhs += '请选择 '
          }
          lhs += this.$lhs.getText(row.condition, false, false)
          ruleArr.push(lhs)
          ruleArr.push('那么')
          let rhs = ''
          if (row.scoreLabel) {
            rhs += row.scoreLabel + ' = '
          } else {
            rhs += '分值 = '
          }
          if (row.weight) {
            rhs += '('
          }
          if (row.score) {
            rhs += this.$lhs.getExpressionText(row.score)
          } else {
            rhs += '请选择'
          }
          if (row.weight) {
            rhs += ') * ' + row.weight
          }
          rhs += '\n'
          if (row.reasonCode) {
            rhs += '原因码 = ' + row.reasonCode
            rhs += '\n'
          }
          if (row.reasonMsg) {
            rhs += '原因信息 = ' + row.reasonMsg
            rhs += '\n'
          }
          ruleArr.push(rhs)
          textArr.push(ruleArr.join('\n'))
        })
        return textArr.join('\n')
      },
      getScorecardHeaderText(json) {
        const headerTextArr = []
        headerTextArr.push(`支持权重（${json.weight ? '是' : '否'}）`)
        headerTextArr.push(`评分计算方式（${json.scoringType == 'SUM' ? '求和' : '求积'}）`)
        if (json.assign && json.assign.id) {
          const assignText = this.$store.getters.getFactText(json.assign.id, json.assign.fieldId)
          headerTextArr.push(`将评分赋给（${assignText}）`)
        }
        headerTextArr.push(`初始评分（${json.defaultScore ? json.defaultScore : '0'}）`)
        return headerTextArr.join('，') + '\n'
      },
      getAttrsText(attrs) {
        if (attrs.length == 0) {
          return ''
        }

        const attrTextArr = []
        attrs.forEach(e => {
          if (!e.label) {
            if (e.name == 'enabled') {
              e.label = '是否可用'
              e.text = e.value == 'true' ? '是' : '否'
            } else if (e.name == 'salience') {
              e.label = '优先级'
            } else if (e.name == 'date-effective') {
              e.label = '生效日期'
            } else if (e.name == 'date-expires') {
              e.label = '失效日期'
            } else if (e.name == 'activation-group') {
              e.label = '互斥组'
            }
            if (!e.text) {
              e.text = e.value
            }
          }
          attrTextArr.push(`${e.label}（${e.text}）`)
        })
        return attrTextArr.join('，')
      },
      getRhsText(rhs) {
        const rhsArr = []
        rhs.actions.forEach(action => {
          rhsArr.push(this.getActionText(action))
        })
        return rhsArr.join('\n')
      },
      getActionText(action) {
        let text = ''
        if (action.type == 'ASSIGN') {
          const factText = this.$lhs.getFactExpressionText(action.left)
          text += factText + ' = '
          const rightText = this.$lhs.getExpressionText(action.right)
          text += rightText
        } else if (action.type == 'FUNC') {
          text = this.$lhs.getFuncExpressionText(action.func)
        } else if (action.type == 'MODEL') {
          text = this.$lhs.getModelExpressionText(action.model)
        }
        return text
      }
    }
  }
</script>

<style scoped>
  .diffContent {
    display: flex;
    justify-content: space-between;
  }
  .diffContent .diffComponent {
    width: 48%;
  }
  .divider {
    width: 1px;
    border-left: 1px dashed #ccc;
  }
</style>
