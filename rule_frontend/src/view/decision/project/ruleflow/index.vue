<template>
  <div class="addAssets">
    <el-row v-if="!readonly">
      <el-col class="toolbar">
        <save-btn @callback="saveCallback" :assets="assets" @refresh="refresh"></save-btn>
        <test-btn :assets="assets"></test-btn>
        <assets-editor-dialog :assets="assets" @refresh="refresh"></assets-editor-dialog>
        <delete-btn :assets="assets" @delete="uuid => {$emit('delete', uuid)}"></delete-btn>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <div style="width: 100%; display: flex; justify-content: space-between">
          <div :id="'paletteDiv'+date" style="width: 120px; background-color: whitesmoke; border: solid 1px #ddd" v-if="!readonly"></div>
          <div :id="'diagramDiv'+date" style="flex-grow: 1; height: 500px; border: solid 1px #ddd"></div>
          <ul class="el-dropdown-menu el-popper" ref="contextMenu"
              style="top: -1000px; transform-origin: center bottom 0px;">
            <li class="el-dropdown-menu__item" ref="contextMenu-setRule" @click="cxcommand(0)">
              <i class="iconfont iconguize"></i>
              {{readonly ? '查看规则' : '设置规则'}}
            </li>
            <li class="el-dropdown-menu__item" ref="contextMenu-setDecision" @click="cxcommand(1)">
              <i class="iconfont iconfenzhijiedian"></i>
              {{readonly ? '查看决策' : '设置决策'}}
            </li>
            <li class="el-dropdown-menu__item" ref="contextMenu-setScript" @click="cxcommand(2)">
              <i class="iconfont iconjiaobenrenwu"></i>
              {{readonly ? '查看脚本' : '设置脚本'}}
            </li>
            <li class="el-dropdown-menu__item" ref="contextMenu-delNode" @click="cxcommand(3)">
              <i class="el-icon-delete"></i>
              删除节点
            </li>
          </ul>
        </div>
      </el-col>
    </el-row>

    <el-dialog title="规则文件列表" :top="$dialogTop" width="60%" :visible.sync="showRuleEditDialog" append-to-body>
      <el-button type="primary" icon="el-icon-plus" @click="addRule" :disabled="rules.length > 0" v-if="!readonly">添加规则文件</el-button>
      <el-table :data="rules">
        <el-table-column prop="name" label="文件名称"></el-table-column>
        <el-table-column prop="versionNo" label="版本号">
          <template
            slot-scope="scope"
          >{{scope.row.versionNo||(scope.row.versionNo==0)?'V'+scope.row.versionNo:""}}</template>
        </el-table-column>
           <el-table-column label="版本描述">
              <template slot-scope="scope" v-if="scope.row.versionDes">
                <span v-if="scope.row.versionDes.length<12">{{ scope.row.versionDes }}</span>
                <el-tooltip v-else placement="top" :content="scope.row.versionDes">
                  <span>{{ (scope.row.versionDes.length>12)?scope.row.versionDes.substring(0,12)+"...":scope.row.versionDes }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
          <el-table-column label="创建时间">
              <template
              v-if="scope.row.createTime"
                slot-scope="scope"
              >{{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}
              </template>
        </el-table-column>
        <el-table-column prop="type" label="文件类型"
                         :formatter="row => {return this.$utils.getAssetsTypeText(row.type)}"></el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button @click.stop="viewAssets(scope.row)" type="text">查看</el-button>
            <el-button @click.stop="delRule(scope)" type="text" v-if="!readonly">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="showRuleEditDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" @click="submitEditRule" icon="el-icon-success" v-if="!readonly">确 定</el-button>
      </div>

      <el-dialog title="添加规则文件" :top="$dialogTop" width="60%" :visible.sync="showRuleAddDialog" append-to-body @closed="closedSearch">
          <div class="searchContent">
            <el-input placeholder="搜索" style="width:220px;" v-model="searchName" class="search"
                      @keyup.enter.native="search">
              <i slot="suffix" class="el-input__icon el-icon-search" @click="search"></i>
            </el-input>
          </div>
        <el-tabs type="border-card" class="tbContent">
          <el-tab-pane v-for="ag in ruleGroup" :key="ag.type" :label="ag.type">
            <el-table
            class="tb-edit"
            :data="ag.list"
            style="height:300px; overflow-y: auto;"
            highlight-current-row
            @cell-click="cellClick">
              <el-table-column prop="name" label="文件名称"></el-table-column>
              <el-table-column label="版本号" align="center" style="width: 60%;">
                  <template slot-scope="scope">
                    <el-select
                      v-model="scope.row.select"
                      :ref="'input_original_' + scope.row.uuid"
                      placeholder="请选择"
                      class="scopSelect"
                       @change="selectVerion(scope.row)">
                      <el-option :value="item.versionNo" :label="'V'+item.versionNo" v-for="(item,index) in scope.row.assetsVersions" :key="index"></el-option>
                    </el-select>
                    <span class="originSelect">{{'V'+scope.row.select}}</span>
                  </template>
             </el-table-column>
             <el-table-column label="版本描述">
              <template slot-scope="scope" v-if="scope.row.versionDes">
                <span v-if="scope.row.versionDes.length<12">{{ scope.row.versionDes }}</span>
                <el-tooltip v-else placement="top" :content="scope.row.versionDes">
                  <span>{{ (scope.row.versionDes.length>12)?scope.row.versionDes.substring(0,12)+"...":scope.row.versionDes }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
              <el-table-column label="创建时间">
              <template slot-scope="scope">
                {{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}
              </template>
            </el-table-column>
              <el-table-column label="操作" width="120">
                <template slot-scope="scope">
                  <el-button @click.stop="viewAssets(scope.row)" type="text">查看</el-button>
                  <el-button @click.stop="submitAddRule(scope.row, scope.$index, ag.list)" type="text">添加</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>
    </el-dialog>

    <el-dialog :title="readonly ? '查看决策' : '设置决策'" :visible.sync="showDecisionEditDialog" append-to-body>
      <el-form :model="decisionForm" label-width="80px" :inline="true">
        <el-form-item label="分支类型">
          <el-select v-model="decisionForm.type" placeholder="请选择" :disabled="readonly">
            <el-option label="条件" value="CONDITION"></el-option>
            <el-option label="百分比" value="PERCENTAGE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="策略配置" v-if="decisionForm.type == 'CONDITION'">
          <el-select v-model="decisionForm.strategy" placeholder="请选择" :disabled="readonly">
            <el-option label="XOR" value="XOR"></el-option>
            <el-option label="OR" value="OR"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="decisionForm.connections" v-if="showDecisionEditDialog">
        <el-table-column :label="decisionForm.type == 'CONDITION' ? '条件' : '百分比(单位%)'">
          <template slot-scope="scope">
            <template v-if="decisionForm.type == 'CONDITION'">
              <template v-if="!readonly">
                <el-popover @show="$refs[`lhs_${scope.$index}`].draw()">
                  <lhs v-model="scope.row.lhs" :ref="'lhs_' + scope.$index"></lhs>
                  <el-button type="text" slot="reference" style="white-space: normal; line-height: 1.5;">
                    <span v-html="$lhs.getText(scope.row.lhs, true)"></span>
                  </el-button>
                </el-popover>
              </template>
              <template v-else>
                <span v-html="$lhs.getText(scope.row.lhs, true)" style="white-space: normal; line-height: 1.5;"></span>
              </template>
            </template>
            <template v-else>
              <el-input-number
                v-model="scope.row.percentage"
                :controls="false"
                :disabled="readonly"
                :precision="0"
                :min="0"
                :max="maxPercentage(scope.$index)">
              </el-input-number>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="流向">
          <template slot-scope="scope">
            <el-select v-model="scope.row.key" placeholder="请选择" :disabled="readonly">
              <el-option v-for="node in decisionForm.toNodes" :key="node.key" :label="node.name"
                         :value="node.key"></el-option>
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="showDecisionEditDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" @click="submitEditDecision" icon="el-icon-success" v-if="!readonly">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="readonly ? '查看脚本' : '设置脚本'" :visible.sync="showScriptEditDialog" append-to-body>
      <rhs v-model="script.rhs" :from-flow="true" :readonly="readonly"></rhs>
      <div slot="footer">
        <el-button @click="showScriptEditDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" @click="submitEditScript" icon="el-icon-success" v-if="!readonly">确 定</el-button>
      </div>
    </el-dialog>

    <assets-view :assets-uuid="viewingAssets.uuid" :assets-version="viewingAssets.version" :show.sync="viewingAssets.show"></assets-view>
  </div>
</template>

<script>
  import lhs from '../components/lhs'
  import rhs from '../components/rhs'
  import op from '../components/op'
  import deleteBtn from '../assets/delete-btn'
  import testBtn from '../assets/test-btn'

  const $ = go.GraphObject.make

  export default {
    name: "index",
    components: {
      lhs, rhs, op,
      'delete-btn': deleteBtn,
      'test-btn': testBtn,
    },
    props: {
      assets: Object,
      readonly: {
        type: Boolean,
        default: false
      },
      fireRules: {
        type: Array,
        default: function() {
          return []
        }
      }
    },
    watch: {
      readonly: function(newVal, oldVal) {
        if (oldVal === true && newVal !== true) {
          // 解锁文件
          this.$nextTick(() => {
            this.initPalette()
            this.diagram.isReadOnly = false
          })
        }
      }
    },
    data() {
      return {
        saveLoading: false,
        palette: null,
        diagram: null,
        flow: {},
        showRuleEditDialog: false,
        showRuleAddDialog: false,
        showDecisionEditDialog: false,
        rules: [],
        ruleGroup: [],
        activeNode: null,
        decisionForm: {},
        showScriptEditDialog: false,
        script: {},
        date: (new Date()).valueOf(),
        saveVersionLoading: false,
        showNewVersionDialog: false,
        searchName:"",
        sName:"",
        sDirName:"",
        nowVersion:[],
        targetVersion:[],
        viewingAssets: {
          uuid: '',
          version: 0,
          show: false
        }
      }
    },
    mounted() {
      // Define a function for creating a "port" that is normally transparent.
      // The "name" is used as the GraphObject.portId,
      // the "align" is used to determine where to position the port relative to the body of the node,
      // the "spot" is used to control how links connect with the port and whether the port
      // stretches along the side of the node,
      // and the boolean "output" and "input" arguments control whether the user can draw links from or to the port.
      function makePort(name, align, spot, output, input) {
        var horizontal = align.equals(go.Spot.Top) || align.equals(go.Spot.Bottom);
        // the port is basically just a transparent rectangle that stretches along the side of the node,
        // and becomes colored when the mouse passes over it
        return $(go.Shape,
          {
            fill: "transparent",  // changed to a color in the mouseEnter event handler
            strokeWidth: 0,  // no stroke
            width: horizontal ? NaN : 8,  // if not stretching horizontally, just 8 wide
            height: !horizontal ? NaN : 8,  // if not stretching vertically, just 8 tall
            alignment: align,  // align the port on the main Shape
            stretch: (horizontal ? go.GraphObject.Horizontal : go.GraphObject.Vertical),
            portId: name,  // declare this object to be a "port"
            fromSpot: spot,  // declare where links may connect at this port
            fromLinkable: output > 0,  // declare whether the user may draw links from here,
            fromMaxLinks: output,
            toSpot: spot,  // declare where links may connect at this port
            toLinkable: input > 0,  // declare whether the user may draw links to here
            toMaxLinks: input,  // declare whether the user may draw links to here
            cursor: "pointer",  // show a different cursor to indicate potential link point
            mouseEnter: function (e, port) {  // the PORT argument will be this Shape
              if (!e.diagram.isReadOnly) port.fill = "rgba(255,0,255,0.5)";
            },
            mouseLeave: function (e, port) {
              port.fill = "transparent";
            }
          });
      }

      this.initData()
      this.initDiagram()
      if (!this.readonly) {
        this.initPalette()
      }
    },
    methods: {
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
      initData() {
        if (this.assets.content) {
          this.flow = JSON.parse(this.assets.content)
        } else {
          this.flow = {}
        }
      },
      initDiagram() {
        const diagram = $(go.Diagram, ('diagramDiv' + this.date),
          {
            initialContentAlignment: go.Spot.Center,
            allowDrop: true,
            scrollsPageOnFocus: false,
            isReadOnly: this.readOnly === true,
            "grid.gridCellSize": new go.Size(30, 20),
            "draggingTool.isGridSnapEnabled": true,
            "resizingTool.isGridSnapEnabled": true,
            "rotatingTool.snapAngleMultiple": 90,
            "rotatingTool.snapAngleEpsilon": 45,
            "undoManager.isEnabled": true,
            ExternalObjectsDropped: (e) => {
              let startCount = 0
              for (var it = diagram.nodes.iterator; it.next();) {
                if (it.value.data.category == 'Start') startCount++
              }
              let existStartNode
              for (var it = e.subject.iterator; it.next();) {
                if (it.value.data.category == 'Start' && startCount > 1) {
                  existStartNode = true
                }
              }
              if (existStartNode) {
                diagram.commandHandler.deleteSelection()
                this.$message.warning('只能有一个开始节点')
              }
            }
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

        const self = this

        function showContextMenu(obj, diagram, tool) {
          const category = obj.part.data.category
          if (self.readonly && (category == 'Start' || category == 'Diverge' || category == 'Converge')) return

          if (diagram.div.id == ('paletteDiv' + self.date)) return
          self.activeNode = obj.part
          self.$refs['contextMenu-setRule'].style.display = category == 'Rule' ? 'block' : 'none'
          self.$refs['contextMenu-setDecision'].style.display = category == 'Decision' ? 'block' : 'none'
          self.$refs['contextMenu-setScript'].style.display = category == 'Script' ? 'block' : 'none'
          if (self.readonly) {
            self.$refs['contextMenu-delNode'].style.display = 'none'
          }
          // we don't bother overriding positionContextMenu, we just do it here:
          const mousePt = diagram.lastInput.viewPoint;
          let x = mousePt.x, y = mousePt.y
          if (x + cxElement.offsetWidth > diagram.div.offsetWidth) {
            x -= cxElement.offsetWidth
          }
          if (y + cxElement.offsetHeight > diagram.div.offsetHeight) {
            y -= cxElement.offsetHeight
          }
          if (!self.readonly) {
            cxElement.style.left = (x + 120) + "px";
          } else {
            cxElement.style.left = x + "px";
          }
          cxElement.style.top = y + "px";
        }

        diagram.nodeTemplateMap.add('Start',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
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
              $(go.TextBlock, '开始', self.textStyle(), {editable: true}, new go.Binding('text', 'name').makeTwoWay()),
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
                  wrap: go.TextBlock.WrapFit,
                  editable: true
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
                  wrap: go.TextBlock.WrapFit,
                  editable: true
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Diverge',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
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
                  wrap: go.TextBlock.WrapFit,
                  editable: true
                },
                new go.Binding('text', 'name').makeTwoWay()
              )
            )
          )
        )

        diagram.nodeTemplateMap.add('Converge',
          $(go.Node, 'Table', self.nodeStyle(), {contextMenu: myContextMenu},
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
                  wrap: go.TextBlock.WrapFit,
                  editable: true
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
                  wrap: go.TextBlock.WrapFit,
                  editable: true
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
        diagram.toolManager.linkingTool.linkValidation = (fromNode, fromGo, toNode, toGo, link) => {
          let valid = true
          for (var it = fromNode.linksConnected.iterator; it.next();) {
            if (it.value.fromNode == toNode || it.value.toNode == toNode) {
              valid = false
              break
            }
          }
          return valid
        }
        const model = go.Model.fromJson(this.flow)
        diagram.model = $(go.GraphLinksModel, {
          linkFromPortIdProperty: 'fromPort',
          linkToPortIdProperty: 'toPort',
          nodeDataArray: model.nodeDataArray,
          linkDataArray: model.linkDataArray
        })

        const commandHandler = diagram.commandHandler
        // 粘贴节点跟随鼠标
        commandHandler.pasteSelection = pos => {
          if (!pos) {
            pos = diagram.lastInput.documentPoint
          }
          go.CommandHandler.prototype.pasteSelection.call(commandHandler, pos)
        }

        this.diagram = diagram

        // 高亮当前触发节点
        if (this.fireRules.length > 0) {
          this.fireRules.forEach(rule => {
            const key = rule.substring(rule.lastIndexOf('-'))
            const part = diagram.findNodeForKey(key)
            if (part) {
              const ad = $(go.Adornment, 'Auto',
                $(go.Shape, {fill: null, stroke: 'red', strokeWidth: 3}),
                $(go.Placeholder, {margin: 0})
              ).copy()
              ad.adornedObject = part
              ad.location = part.position
              part.addAdornment('diff', ad)
            }
          })
        }
      },
      initPalette() {
        const self = this;
        this.palette = $(go.Palette, ('paletteDiv' + self.date),
          {
            scrollsPageOnFocus: false,
            nodeTemplateMap: this.diagram.nodeTemplateMap,
            model: $(go.GraphLinksModel, {
              linkFromPortIdProperty: 'fromPort',
              linkToPortIdProperty: 'toPort',
              nodeDataArray: [
                {category: 'Start', name: '开始'},
                {category: 'Rule', name: '规则'},
                {category: 'Script', name: '脚本'},
                {category: 'Decision', name: '决策'},
                {category: 'Diverge', name: '分支'},
                {category: 'Converge', name: '聚合'},
              ]
            })
          }
        )
      },
      cxcommand(i) {
        if (i == 0) {
          this.editRule()
        } else if (i == 1) {
          this.editDecision()
        } else if (i == 2) {
          this.editScript()
        } else if (i == 3) {
          this.diagram.commandHandler.deleteSelection()
        }
        this.hideContextMenu()
      },
      editRule() {
        if (this.activeNode.data.rules) {
          // copy一份数据，解决复制节点同时复制数据，修改冲突
          this.rules = this.$utils.copy(this.activeNode.data.rules)
        } else {
          this.rules = []
        }
        this.showRuleEditDialog = true
      },
      cellClick(row, column, cell, event) {
        const el = this.$refs["input_original_" + row.uuid][0];
        if (column.label == "版本号" && el) {
          this.$nextTick(function() {
            el.toggleMenu();
          });
        }
      },
      addRule() {
        this.showRuleAddDialog = true
        this.$axios.get('/assets/query', {
          params: {
            projectUuid: this.assets.projectUuid,
            name: this.searchName,
          }
        }).then(res => {
          if (res.data.code == 0) {
            this.ruleGroup = []
            res.data.data.forEach(e => {
              if (e.type == 'fact' || e.type == 'constant') {
                return
              }
              e.list.forEach(item=>{
                item.select = item.assetsVersions[0].versionNo;
                item.versionDes = item.assetsVersions[0].versionDes;
                item.createTime = item.assetsVersions[0].createTime;
              })
              if (e.type == 'ruleflow') {
                e.list = e.list.filter(f => f.uuid != this.assets.uuid)
              }
              // 过滤掉已经添加的规则文件
              e.list = e.list.filter(f => {
                let exists = false
                this.rules.some(r => {
                  if (f.uuid == r.uuid) {
                    exists = true
                    return true
                  }
                })
                return !exists
              })
              e.type = this.$utils.getAssetsTypeText(e.type)
              this.ruleGroup.push(e)
            })
          }
        })
      },
      submitEditRule() {
        this.activeNode.data.rules = this.rules
        this.showRuleEditDialog = false
      },
      search(){
        this.addRule()
      },
      closedSearch() {
        this.searchName=""
        this.sName=""
        this.sDirName=""
        this.checkList = []
      },
      submitAddRule(row, i, list) {
        if (row.type == 'ruleflow') { // 一个规则节点只能添加一个决策流
          let hasRuleFlow = false
          this.rules.some(e => {
            if (e.type == 'ruleflow') {
              hasRuleFlow = true
              return true
            }
          })
          if (hasRuleFlow) {
            this.$message.warning('一个规则节点只能添加一个决策流')
            return
          }
        }

        this.rules.push({
          uuid: row.uuid,
          name: row.name,
          type: row.type,
          versionNo: row.select,
          versionDes: row.versionDes,
          createTime: row.createTime,
        })
        list.splice(i, 1)
        this.$message.success('添加成功')
        this.showRuleAddDialog = false
      },
      delRule(scope) {
        this.rules.splice(scope.$index, 1)
      },
      selectVerion(row) {
        row.assetsVersions.forEach(item => {
          if (row.select == item.versionNo) {
            row.versionDes = item.versionDes;
            row.createTime = item.createTime;
          }
        });
      },
      viewAssets(row) {
        this.viewingAssets.uuid = row.uuid
        this.viewingAssets.version = row.select ? row.select : row.versionNo
        this.viewingAssets.show = true
      },
      editDecision() {
        if (!this.activeNode.data.decision) this.activeNode.data.decision = {}
        this.$set(this.decisionForm, 'type', this.activeNode.data.decision.type || 'CONDITION')
        this.$set(this.decisionForm, 'strategy', this.activeNode.data.decision.strategy || 'XOR')

        let connections = [], toNodes = []
        for (var it = this.activeNode.findLinksOutOf().iterator; it.next();) {
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
        this.showDecisionEditDialog = true
      },
      maxPercentage(idx) {
        let max = 100
        if (this.decisionForm.type == 'PERCENTAGE') {
          this.decisionForm.connections.forEach((e, i) => {
            if (idx != i && e.percentage) {
              max -= e.percentage
            }
          })
        }
        return max
      },
      submitEditDecision() {
        const connections = this.decisionForm.connections
        if (connections.length > 1) {
          for (let i = 0; i < connections.length; i++) {
            for (let j = i + 1; j < connections.length; j++) {
              if (connections[i].key == connections[j].key) {
                this.$message.warning('决策流向不能重复')
                return false
              }
            }
          }
        }

        let decision = this.$utils.copy(this.decisionForm)
        decision.connections.forEach(e => {
          if (decision.type == 'CONDITION') {
            delete e.percentage
          } else {
            delete e.lhs
          }
        })
        this.activeNode.data.decision = decision
        this.showDecisionEditDialog = false
        this.decisionForm = {}
      },
      editScript() {
        if (this.activeNode.data.script) {
          this.script = this.$utils.copy(this.activeNode.data.script)
        } else {
          this.script = {}
        }
        this.showScriptEditDialog = true
      },
      submitEditScript() {
        this.activeNode.data.script = this.script
        this.showScriptEditDialog = false
      },
      getInputType(left, op) {
        if (!left || !left.field) return null

        if (op === '字符串长度为' || op === '字符串长度不为') {
          return 'Integer'
        } else {
          const fact = this.$store.getters.getFactById(left.id, left.field.id)
          if (fact && fact.fields.length > 0) {
            return fact.fields[0].type
          }
        }
      },
      saveCallback(callback) {
        this.flow = JSON.parse(this.diagram.model.toJSON())
        callback(JSON.stringify(this.flow))
      },
      refresh(assets) {
        if (assets.changeVersion === true) {
          this.flow = JSON.parse(assets.content)
          const model = go.Model.fromJson(this.flow)
          this.diagram.model = $(go.GraphLinksModel, {
            linkFromPortIdProperty: 'fromPort',
            linkToPortIdProperty: 'toPort',
            nodeDataArray: model.nodeDataArray,
            linkDataArray: model.linkDataArray
          })
        } else {
          this.$emit('refresh', assets)
        }
      },
      hideContextMenu() {
        this.$refs.contextMenu.style.top = '-1000px'
      }
    }
  }
</script>

<style scoped>
  .searchContent {
    display: flex;
    margin-bottom: 10px;
    align-items: center;
  }
  .tb-edit .el-input,
  .tb-edit .el-input-number,
  .tb-edit .el-select,
  .tb-edit .el-dropdown {
    display: none;
  }

  .tb-edit .current-row .el-input,
  .tb-edit .current-row .el-input-number,
  .tb-edit .current-row .el-select,
  .tb-edit .current-row .el-select + .el-dropdown {
    display: inline-block;
  }

  .tb-edit .current-row .el-input + span,
  .tb-edit .current-row .el-input-number + span,
  .tb-edit .current-row .el-select + span,
  .tb-edit .current-row .el-dropdown + span {
    display: none;
  }
  .addFile {
    font-size: 16px;
    margin-bottom: 6px;
  }
  .addFile .title {
    color: #909399;
  }
  .addFile .selectAsset {
    cursor: pointer;
    color: #409eff;
  }
  .testcase .activeClass {
    color: #f56c6c;
  }
  .originSelect {
    cursor: pointer;
    color: #409eff;
  }
  .scopSelect {
    width: 120px;
  }
  .tbContent >>> .el-table:before {
    left: 0;
    bottom: 0;
    width: 100%;
    height: 0px;
  }
</style>
