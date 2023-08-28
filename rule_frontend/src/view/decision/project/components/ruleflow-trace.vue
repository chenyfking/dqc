<template>
  <el-dialog
    title="查询轨迹"
    :visible="show"
    @update:visible="close"
    @opened="init"
    v-loading="loading"
    element-loading-text="数据加载中..."
    width="80%"
    top="10px"
    append-to-body>
    <div v-if="show" id="diagramDiv" style="flex-grow: 1; height: 500px; border: solid 1px #ddd;"></div>

    <div slot="footer">
      <el-button @click="close" icon="el-icon-error">关 闭</el-button>
    </div>

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

    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show">
    </assets-view>
  </el-dialog>
</template>

<script>
  import rhs from './rhs'

  const $ = go.GraphObject.make
  export default {
    name: "ruleflow-trace",
    props: {
      flowUuid: String,
      flowVersion: Number,
      nodes: Array,
      show: Boolean
    },
    components: {
      rhs
    },
    data() {
      return {
        loading: false,
        flow: null,
        diagram: null,
        linkDataArray: {},
        activeNode: null,
        showRuleDialog: false,
        rules: [],
        showScriptDialog: false,
        script: {},
        decisionForm: {},
        showDecisionDialog: false,
        viewingAssets: {
          uuid: '',
          version: 0,
          show: false
        }
      }
    },
    methods: {
      init() {
        this.loading = true
        this.$axios.get(`/assets/${this.flowUuid}?version=${this.flowVersion}`).then(res => {
          this.loading = false
          if (res.data.code == 0) {
            this.flow = res.data.data
            this.initDiagram()
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '查询失败')
          }
        }).catch(() => {
          this.$message.error('查询失败')
          this.loading = false
        })
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
      initDiagram() {
        const self = this
        const diagram = $(go.Diagram, 'diagramDiv',
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
          cxElement.style.left = (x + 20) + "px";
          cxElement.style.top = (y + 80) + "px";
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
          $(
            go.Shape,  // the highlight shape, normally transparent
            {isPanelMain: true, strokeWidth: 8, stroke: "transparent", name: "HIGHLIGHT"}
          ),
          $(
            go.Shape,  // the link path shape
            {isPanelMain: true, stroke: "gray", strokeWidth: 2},
            new go.Binding("stroke", "isHighlighted", function (h) {
              return h ? "red" : "#ff";
            }).ofObject(),
            new go.Binding("strokeWidth", "isHighlighted", function (h) {
              return h ? 4 : 2
            }).ofObject(),
            new go.Binding("strokeWidth", "isSelected", function (sel) {
              return sel ? 'dodgerblue' : 'gray'
            }).ofObject()
          ),
          $(
            go.Shape,  // the arrowhead
            {toArrow: "standard", strokeWidth: 0, fill: "gray"},
            new go.Binding("stroke", "isHighlighted", function (h) {
              return h ? "red" : "#ff";
            }).ofObject(),
            new go.Binding("strokeWidth", "isHighlighted", function (h) {
              return h ? 4 : 2
            }).ofObject()
          ),
          $(
            go.Panel, "Auto",  // the link label, normally not visible
            {name: "LABEL", segmentIndex: 2, segmentFraction: 0.5},
            new go.Binding("visible", "visible").makeTwoWay(),
            $(
              go.Shape, "RoundedRectangle",  // the label shape
              {visible: false, fill: "#F8F8F8", strokeWidth: 0}
            ),
            $(
              go.TextBlock,  // the label
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
        const model = go.Model.fromJson(this.flow.content)
        diagram.model = $(go.GraphLinksModel, {
          linkFromPortIdProperty: 'fromPort',
          linkToPortIdProperty: 'toPort',
          nodeDataArray: model.nodeDataArray,
          linkDataArray: model.linkDataArray
        })

        this.linkDataArray = model.linkDataArray
        this.diagram = diagram
        this.diagram.addDiagramListener('InitialLayoutCompleted', function() {
          self.highLight()
        })
      },
      highLight() {
        this.diagram.startTransaction("highlight");
        this.diagram.clearHighlighteds();
        this.linkDataArray.forEach(link => {
          const fromKey = link.from
          const toKey = link.to
          const from = this.nodes.indexOf(fromKey)
          const to = this.nodes.indexOf(toKey)
          if (from != -1 && to != -1) {
            const fromNode = this.diagram.findNodeForKey(fromKey)
            const toNode = this.diagram.findNodeForKey(toKey)
            fromNode.findLinksTo(toNode).each(function(link) {
              link.isHighlighted = true
            })
          }
        })
        this.diagram.commitTransaction("highlight");
      },
      close() {
        this.$emit('update:show', false)
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
      hideContextMenu() {
        this.$refs.contextMenu.style.top = '-1000px'
      }
    }
  }
</script>

<style scoped>

</style>
