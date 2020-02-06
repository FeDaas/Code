#include "connectionsocket.h"
#include "connection.h"

#include <QtWidgets>
#include <QDebug>

ConnectionSocket::ConnectionSocket(QGraphicsItem *parent, int _direction, QColor _color, QPointF _pos)
{
    Q_UNUSED(parent)

    color = _color;
    direction = _direction;

    setFlags(ItemIsSelectable);
    setAcceptHoverEvents(true);

    setPos(_pos);
}

QRectF ConnectionSocket::boundingRect() const
{
    return QRectF(pos().x(), pos().y(), 12, 12);
}

void ConnectionSocket::paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget)
{
    Q_UNUSED(widget)
    Q_UNUSED(option)

    QColor innerColor = (hovered || connection != nullptr) ? color.darker(80) : color.darker(150);
    QRectF rec = boundingRect();

    painter->setRenderHint(QPainter::Antialiasing);
    QPainterPath path;
    path.addEllipse(rec);

    QPen pen(color, 2);
    painter->setPen(pen);
    painter->fillPath(path, QBrush(innerColor));
    painter->drawPath(path);
}

void ConnectionSocket::addConnection(Connection *connection)
{
    this->connection = connection;
}

void ConnectionSocket::removeConnection()
{
    connection = nullptr;
    update();
}

void ConnectionSocket::nodeMoved()
{
    if (connection != nullptr)
    {
        connection->trackSockets();
    }
}

void ConnectionSocket::trackSignal(QPointF pos)
{
    if (connection != nullptr && (connection->fromSocket() == nullptr || connection->toSocket() == nullptr))
    {
        connection->trackCursor(pos);
    }
}

int ConnectionSocket::type() const
{
    return Type;
}

int ConnectionSocket::getDirection()
{
    return direction;
}

bool ConnectionSocket::hasConnectionObject()
{
    return connection == nullptr ? false : true;
}

bool ConnectionSocket::isConnected()
{
    if (connection != nullptr && connection->fromSocket() != nullptr && connection->toSocket() != nullptr)
    {
        return true;
    }
    return false;
}

Connection* ConnectionSocket::getConnection()
{
    return connection;
}

QPointF ConnectionSocket::scenePos()
{
    QPointF parent_pos = parentItem()->pos();

    qreal pos_x = parent_pos.x() + 2*pos().x() + boundingRect().width() / 2;  // REMINDER Why does pos() have to be multiplied by 2?
    qreal pos_y = parent_pos.y() + 2*pos().y() + boundingRect().height() / 2;

    return QPointF(pos_x, pos_y);
}

bool ConnectionSocket::possibleConnection(ConnectionSocket *socket)
{
    if (socket->parentItem() != parentItem())
    {
        return connection->tryConnect(socket);
    }
    return false;
}

void ConnectionSocket::mousePressEvent(QGraphicsSceneMouseEvent *event)
{
    pressed = true;
    QGraphicsItem::mousePressEvent(event);
}

void ConnectionSocket::mouseReleaseEvent(QGraphicsSceneMouseEvent *event)
{
    pressed = false;
    QGraphicsItem::mouseReleaseEvent(event);
}

void ConnectionSocket::mouseMoveEvent(QGraphicsSceneMouseEvent *event)
{
    QGraphicsItem::mouseMoveEvent(event);
}

void ConnectionSocket::hoverEnterEvent(QGraphicsSceneHoverEvent *event)
{
    hovered = true;
    QGraphicsItem::hoverEnterEvent(event);
}

void ConnectionSocket::hoverLeaveEvent(QGraphicsSceneHoverEvent *event)
{
    hovered = false;
    QGraphicsItem::hoverLeaveEvent(event);
}

QVariant ConnectionSocket::itemChange(GraphicsItemChange change, const QVariant &value)
{
    if (change == ItemPositionHasChanged && connection != nullptr)
    {
        connection->trackSockets();
    }

    return QGraphicsItem::itemChange(change, value);
}
