#include "connection.h"

#include <QPen>
#include <QGraphicsSceneMouseEvent>
#include <QPainter>
#include <QPainterPath>
#include <QDebug>

#include "connectionsocket.h"

Connection::Connection(ConnectionSocket *fromSocket, ConnectionSocket *toSocket)
{
    _fromSocket = fromSocket;
    _toSocket = toSocket;

    if (_fromSocket != nullptr)
    {
        _fromSocket->addConnection(this);
    }

    if (_toSocket != nullptr)
    {
        _toSocket->addConnection(this);
    }

    setColor(Qt::white);
}

Connection::~Connection()
{
    if (_fromSocket != nullptr)
        _fromSocket->removeConnection();

    if (_toSocket != nullptr)
        _toSocket->removeConnection();
}

ConnectionSocket* Connection::fromSocket() const
{
    return _fromSocket;
}

ConnectionSocket* Connection::toSocket() const
{
    return _toSocket;
}

void Connection::setFromSocket(ConnectionSocket *socket)
{
    _fromSocket = socket;
}

void Connection::setToSocket(ConnectionSocket *socket)
{
    _toSocket = socket;
}

void Connection::paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget)
{
    Q_UNUSED(option)
    Q_UNUSED(widget)

    painter->setRenderHint(QPainter::Antialiasing);
    painter->setPen(pen());

    QPointF start;
    QPointF end;

    start=line().p1();
    end=line().p2();

    QPointF mid = (QPointF(start.x() - ((start.x() - end.x()) / 2),
                           start.y() - ((start.y() - end.y()) / 2)));

    QPainterPath conect(start);
    QPointF c1(mid.x(), start.y());
    QPointF c2(mid.x(), end.y());
    conect.cubicTo(c1, c2, end);
    painter->drawPath(conect);

}

void Connection::setColor(QColor color)
{
    setPen(QPen(color, 5));
}

void Connection::trackSockets()
{
    if (_fromSocket != nullptr && _toSocket != nullptr)
    {
        setLine(QLineF(_fromSocket->scenePos(), _toSocket->scenePos()));
    }
}

void Connection::trackCursor(QPointF pos)
{
    setZValue(800);
    // so the conection draws over nodes while hovering
    if (_toSocket == nullptr && _fromSocket != nullptr)
    {
        if (pos.x() - _fromSocket->scenePos().x() >= 0)
        {
            setLine(QLineF(_fromSocket->scenePos(), pos - QPointF(5, 0)));
        }
        else
        {
            setLine(QLineF(_fromSocket->scenePos(), pos + QPointF(5, 0)));
        }
    }
    if (_fromSocket == nullptr && _toSocket != nullptr)
    {
        if (pos.x() - _toSocket->scenePos().x() >= 0)
        {
            setLine(QLineF(_toSocket->scenePos(), pos - QPointF(5, 0)));
        }
        else
        {
            setLine(QLineF(_toSocket->scenePos(), pos + QPointF(5, 0)));
        }
    }
}

bool Connection::tryConnect(ConnectionSocket *socket)
{
    if (!socket->hasConnectionObject())
    {
        if (_fromSocket != nullptr && socket->getDirection() == 1)
        {
            _toSocket = socket;
            socket->addConnection(this);
        }
        else if (_toSocket != nullptr && socket->getDirection() == 0)
        {
            _fromSocket = socket;
            socket->addConnection(this);
        }

        trackSockets();
        setZValue(-1);
        // so the connection draws behind nodes while conevted
        return true;
    }

    return false;
}
