#include "nodescene.h"
#include "nodegeometry.h"
#include "connectionsocket.h"
#include "connection.h"

#include <QKeyEvent>
#include <QGraphicsSceneMouseEvent>
#include <QDebug>

NodeScene::NodeScene(QObject *parent)
{
    Q_UNUSED(parent)
    snap = true;
}

NodeScene::~NodeScene()
{
    QList<QGraphicsItem *> all_items = items();
    for (QGraphicsItem *item : qAsConst(all_items))
    {
        removeItem(item);
        delete item;
    }
}

void NodeScene::addNode(QPointF pos, QString name, int inputs, int outputs)
{
    // All values are only placeholders for now

    NodeGeometry *geometry = new NodeGeometry(name,&snap, QColor(128, 128, 128, 200), 200, 100, inputs, outputs);
    addItem(geometry);

    for (int i = 0; i < inputs; ++i)
    {
        ConnectionSocket *socket = new ConnectionSocket(nullptr, 1);
        addItem(socket);
        socket->setParentItem(geometry);
        socket->setPos(QPointF(-3, 25 + i*12));
    }

    for (int i = 0; i < outputs; ++i)
    {
        ConnectionSocket *socket = new ConnectionSocket(nullptr, 0);
        addItem(socket);
        socket->setParentItem(geometry);
        socket->setPos(QPointF(97, 25 + i*12));
    }

    geometry->setPos(pos);
    geometry->snapActivate();
}

void NodeScene::addConnection()
{
    Connection *newConnection;
    if (activeSocket->getDirection() == 0)
    {
        newConnection = new Connection(activeSocket, nullptr);
    }
    else
    {
        newConnection = new Connection(nullptr, activeSocket);
    }

    addItem(newConnection);
}

void NodeScene::keyPressEvent(QKeyEvent *event)
{
    if (activeSocket == nullptr && (event->key() == Qt::Key_Delete || event->key() == Qt::Key_Backspace))
    {
        QList<QGraphicsItem *> selected_items = selectedItems();

        for (QGraphicsItem *item : qAsConst(selected_items))
        {
            if (item->type()==NodeGeometry::Type)
            {
                for (QGraphicsItem *child : item->childItems())
                {
                    ConnectionSocket *socket = qgraphicsitem_cast<ConnectionSocket *>(child);
                    Connection *connection = socket->getConnection();
                    if (connection != nullptr)
                    {
                        removeItem(connection);
                        delete connection;
                    }
                }
                removeItem(item);
                delete item;
            }
        }
    }

    QGraphicsScene::keyPressEvent(event);
}

void NodeScene::mouseMoveEvent(QGraphicsSceneMouseEvent *event)
{
    if (activeSocket != nullptr)
    {
        if (!activeSocket->hasConnectionObject())
        {
            addConnection();
        }

        activeSocket->trackSignal(event->scenePos());
    }

    QGraphicsScene::mouseMoveEvent(event);
}

void NodeScene::mousePressEvent(QGraphicsSceneMouseEvent *event)
{
    QGraphicsItem *item = itemAt(event->scenePos(), QTransform());

    if (item != nullptr && item->type() == ConnectionSocket::Type)
    {
        activeSocket = qgraphicsitem_cast<ConnectionSocket *>(item);

        if (activeSocket->isConnected())
        {
            Connection *connection = activeSocket->getConnection();
            activeSocket->removeConnection();

            if (activeSocket->getDirection() == 0)
            {
                connection->setFromSocket(nullptr);
            }
            if (activeSocket->getDirection() == 1)
            {
                connection->setToSocket(nullptr);
            }

            activeSocket = (connection->fromSocket() != nullptr) ? connection->fromSocket() : connection->toSocket();
            connection->trackCursor(event->scenePos());
        }
    }

    QGraphicsScene::mousePressEvent(event);
}

void NodeScene::mouseReleaseEvent(QGraphicsSceneMouseEvent *event)
{
    if (activeSocket != nullptr && activeSocket->hasConnectionObject())
    {
        QGraphicsItem *item = itemAt(event->scenePos(), QTransform());

        if (item != nullptr && item->type() == ConnectionSocket::Type)
        {
            ConnectionSocket *socket = qgraphicsitem_cast<ConnectionSocket *>(item);
            activeSocket->possibleConnection(socket);
        }

        if (!activeSocket->isConnected())
        {
            Connection *connection = activeSocket->getConnection();
            removeItem(connection);
            delete connection;
        }

    }

    activeSocket = nullptr;

    update();
    if(snap)
    {
        snapToGrid(true);
    }
    QGraphicsScene::mouseReleaseEvent(event);
}

void NodeScene::toggleSnap()
{
snap=!snap;
}

void NodeScene::snapToGrid(bool onlySelected)
{
    QList<QGraphicsItem *> itemList;

    if(onlySelected)
    {
        itemList = selectedItems();
    }
    else
    {
        itemList = items();
    }

    for (QGraphicsItem *item : itemList)
    {
        if (item->type() == NodeGeometry::Type)
        {
            NodeGeometry *node = qgraphicsitem_cast<NodeGeometry*>(item);
            node->snapActivate();
        }
    }
}

