import Vue from 'vue';
import FactNode from './fact-node';
import ConditionNode from './condition-node';
import ActionNode from './action-node';
import Connection from './connection';

export default class Context {
  constructor(container) {
    this.container = container;
    this.paper = this.initPaper();
    this.width = 0;
    this.height = 0;
    this.nodes = [];
    this.connections = [];
  }

  initPaper() {
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
    svg.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
    svg.setAttribute("version", "1.1");
    svg.setAttribute("height", "100%");
    svg.setAttribute("style", "overflow: hidden; position: relative;");
    this.container.innerHTML = '';
    this.container.appendChild(svg);
    return svg;
  }

  initTree(tree) {
    this.state = 'INITING';
    this.rootNode = new FactNode(this, null, tree);
    this.nodes.push(this.rootNode);
    this.state = 'READY';

    this.traverse();
  }

  traverse() {
    if (this.state != 'READY') return;

    Vue.nextTick(() => {
      this.width = 0;
      this.height = 0;

      this.rootNode.traverse();

      this.connections.forEach(conn => conn.drawPath());
      this.nodes.forEach(node => node.setPosition());

      this.container.style.width = this.width + 5 + 'px';
      this.container.style.height = this.height + 20 + 'px';
      this.paper.setAttribute("width", this.width);
    });
  }

  newNode(data, parentNode) {
    let node;
    if (data.nodeType == 'CONDITION') {
      node = new ConditionNode(this, parentNode, data);
    } else if (data.nodeType == 'ACTION') {
      node = new ActionNode(this, parentNode, data);
    } else {
      node = new FactNode(this, parentNode, data);
    }

    this.nodes.push(node);
    this.connections.push(new Connection(this, parentNode, node));

    return node;
  }

  deleteNode(node) {
    let index = this.nodes.indexOf(node);
    if (index != -1) {
      this.nodes.splice(index, 1);
      index = this.connections.indexOf(node.connection);
      if (index != -1) {
        this.connections.splice(index, 1);
      }
    }
  }

  startTransaction(task, traverseAfterTask = true) {
    this.freeze();
    task();
    this.unfreeze();

    traverseAfterTask && this.traverse();
  }

  freeze() {
    this.state = 'FREEZE';
  }

  unfreeze() {
    this.state = 'READY';
  }

  destroy() {
    this.nodes.forEach(node => node.destroy());
  }
}
